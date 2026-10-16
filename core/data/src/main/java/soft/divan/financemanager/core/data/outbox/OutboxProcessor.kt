package soft.divan.financemanager.core.data.outbox

import soft.divan.financemanager.core.data.source.OutboxLocalDataSource
import soft.divan.financemanager.core.database.entity.OutboxEntryEntity
import java.time.Clock
import javax.inject.Inject

/** Сколько записей берём за один проход, чтобы не занимать сеть надолго. */
private const val BATCH_LIMIT = 50

/**
 * Срок «аренды» записи, взятой в работу.
 *
 * Прогон, взявший запись, может не дожить до доклада об исходе: система убивает фоновый процесс,
 * WorkManager снимает работу по таймауту. По истечении этого срока запись считается брошенной и
 * снова доступна для отправки — иначе она осталась бы в `IN_PROGRESS` навсегда и потерялась молча.
 *
 * Величина выбрана заметно больше самого долгого честного вызова (таймауты OkHttp плюс паузы
 * `RetryInterceptor` — это десятки секунд) и заметно меньше интервала фоновой синхронизации,
 * чтобы брошенная запись не ждала освобождения дольше необходимого.
 */
private const val LEASE_MILLIS = 5 * 60 * 1000L

/**
 * Разбирает очередь исходящих операций: решает, какие записи брать, в каком порядке и когда
 * остановиться. Судьбой отдельной записи занимается [OutboxEntryHandler].
 *
 * Заменяет собой прежний `pushLocalChanges`, который просто перебирал pending-записи и не имел
 * ни счётчика попыток, ни backoff, ни способа остановиться на безнадёжной операции.
 *
 * ### Порядок
 * Операции связаны не все со всеми, а парами: транзакция ждёт свой счёт, правка ждёт своё
 * создание. Ровесники — две транзакции одного счёта — независимы. Основную работу делает барьер в
 * `OutboxDao.getReadyToSend`, который просто не выдаёт запись с незакрытым предшественником.
 *
 * Здесь добавляется то, чего барьер знать не может: предшественник мог застрять **в этом же
 * проходе**, уже после того, как выборка была сделана. Такие записи пропускаются до следующего
 * прогона; независимые от них продолжают уезжать.
 *
 * Терминальные ошибки очередь не блокируют: запись уходит в `FAILED` вместе с зависящими от неё и
 * в следующую выборку не попадает.
 *
 * ### Устойчивость к повторному запуску и к смерти процесса
 * Прогон может стартовать одновременно из WorkManager и по факту новой операции, поэтому каждая
 * запись перед отправкой захватывается атомарно; проигравший захват её пропускает.
 *
 * Захват — это **аренда на [LEASE_MILLIS]**, а не пометка навсегда. Если процесс умрёт во время
 * сетевого вызова, запись останется в `IN_PROGRESS`, но по истечении аренды снова попадёт в
 * выборку и будет отправлена. Без этого её не подобрал бы никто: `FAILED` виден хотя бы в
 * dead-letter, а зависший `IN_PROGRESS` пропал бы совсем — без лога и без счётчика.
 *
 * Повторная отправка после истечения аренды безопасна: операция могла долететь до сервера, но
 * идемпотентность (клиентский id, `404-on-delete`) делает дубль невозможным.
 *
 * Возврат по аренде намеренно **не тратит попытку**: убийство фонового процесса — рядовое событие
 * Android, и списывать за него попытки значило бы отправлять исправные операции в dead-letter.
 */
class OutboxProcessor @Inject internal constructor(
    private val localDataSource: OutboxLocalDataSource,
    private val entryHandler: OutboxEntryHandler,
    private val clock: Clock
) {

    /**
     * Разбирает очередь.
     *
     * Возвращает `true`, если ни одна запись не осталась неотправленной по временной причине, —
     * то есть повторный прогон прямо сейчас не нужен.
     *
     * ### Почему несколько проходов
     * Выборка берётся один раз на проход, а барьер не выдаёт операцию, пока её предшественник не
     * закрыт. Значит, успешная отправка **разблокирует** зависящие от неё операции — но в уже
     * выбранный батч они не попали. Без повторного прохода они ждали бы ближайшего фонового
     * синка: пользователь создал счёт и тут же операцию по нему, а операция уехала бы через час.
     *
     * Поэтому проходы повторяются, пока предыдущий что-то закрыл. [MAX_PASSES] — страховка от
     * бесконечного цикла.
     */
    suspend fun process(): Boolean {
        try {
            // Запись, о судьбе которой в этом прогоне уже решили, повторно не берётся: следующий
            // проход видит свежую выборку и без этого мог бы отправить её второй раз.
            val handled = mutableSetOf<Long>()
            var allDrained = true
            var passes = 0
            var madeProgress = true

            while (madeProgress && passes < MAX_PASSES) {
                val pass = drainOnce(handled)
                allDrained = allDrained && pass.allDrained
                madeProgress = pass.completed > 0 && !pass.runStalled
                passes++
            }
            return allDrained
        } finally {
            // Чистим и при досрочной остановке: иначе уже отправленные записи копились бы в
            // очереди до ближайшего полностью успешного прогона.
            localDataSource.deleteCompleted()
        }
    }

    /** Итог одного прохода: сколько записей закрыто и можно ли продолжать. */
    private data class PassResult(
        val completed: Int,
        val allDrained: Boolean,
        val runStalled: Boolean
    )

    /** Один проход: берёт готовые записи и отправляет их, пропуская зависящие от застрявших. */
    private suspend fun drainOnce(handled: MutableSet<Long>): PassResult {
        val staleBefore = clock.millis() - LEASE_MILLIS
        val ready = localDataSource.getReadyToSend(
            now = clock.millis(),
            staleBefore = staleBefore,
            limit = BATCH_LIMIT
        ).filterNot { it.sequenceNo in handled }

        // Застрявшие в этом проходе: `entityLocalId` — чья операция не уехала, `dependencyKey` —
        // от кого она зависела. Ждущие их записи трогать нельзя, остальные должны уехать.
        val stalledEntities = mutableSetOf<String>()
        val stalledDependencies = mutableSetOf<String>()
        var allDrained = true
        var completed = 0

        for (entry in ready) {
            handled += entry.sequenceNo

            when (claimAndHandle(entry, staleBefore, stalledEntities, stalledDependencies)) {
                OutboxEntryOutcome.DONE -> completed++

                // Пропуск прогрессом не считается: иначе проходы шли бы по кругу
                OutboxEntryOutcome.SKIPPED -> Unit

                OutboxEntryOutcome.STALLED -> {
                    stalledEntities += entry.entityLocalId
                    stalledDependencies += entry.dependencyKey
                    allDrained = false
                }

                // Сеть заблокирована целиком — остальные упрутся в ту же стену
                OutboxEntryOutcome.RUN_STALLED ->
                    return PassResult(completed, allDrained = false, runStalled = true)
            }
        }
        return PassResult(completed, allDrained, runStalled = false)
    }

    /**
     * Пропускает запись, которую брать нельзя, иначе захватывает её и отдаёт на отправку.
     *
     * Проверяются те же три ребра зависимости, что и в барьере выборки: прошлая операция той же
     * строки, операция предшественника ([OutboxEntryEntity.dependencyKey]) и операция того, кто
     * зависит от нас. Барьер этого знать не может — застревание случилось уже после выборки.
     */
    private suspend fun claimAndHandle(
        entry: OutboxEntryEntity,
        staleBefore: Long,
        stalledEntities: Set<String>,
        stalledDependencies: Set<String>
    ): OutboxEntryOutcome = when {
        // Предшественник (или зависящий от нас) застрял в этом же проходе — идти нельзя
        entry.entityLocalId in stalledEntities ||
            entry.dependencyKey in stalledEntities ||
            entry.entityLocalId in stalledDependencies ->
            OutboxEntryOutcome.SKIPPED

        // Запись уже забрал параллельный прогон — не отправляем её второй раз
        !localDataSource.markInProgress(entry.sequenceNo, staleBefore, clock.millis()) ->
            OutboxEntryOutcome.SKIPPED

        else -> entryHandler.handle(entry)
    }

    companion object {
        /**
         * Предел проходов за один разбор очереди.
         *
         * Проход повторяется, пока предыдущий что-то закрыл: успешная отправка разблокирует
         * зависящие от неё операции, а те в уже выбранный батч не попали. Сколько проходов нужно
         * в норме — длина самой длинной цепочки зависимостей: например «счёт → его транзакция →
         * правка этой транзакции» это три прохода. Ровесники разблокируются все разом и лишних
         * проходов не требуют.
         *
         * Предел — страховка от бесконечного цикла. Упёршись в него, разбор просто завершается:
         * оставшееся подберёт следующий прогон, ничего не теряется.
         */
        const val MAX_PASSES = 10
    }
}
