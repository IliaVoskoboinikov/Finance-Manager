package soft.divan.financemanager.core.data.outbox

import soft.divan.financemanager.core.data.source.OutboxLocalDataSource
import soft.divan.financemanager.core.data.transaction.TransactionRunner
import soft.divan.financemanager.core.database.entity.OutboxEntryEntity
import soft.divan.financemanager.core.loggingerror.ErrorLogger
import java.time.Clock
import javax.inject.Inject

/** Сколько записей берём за один прогон, чтобы не занимать сеть надолго. */
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
 * Разбирает очередь исходящих операций: забирает готовые записи по порядку, отправляет их и
 * распоряжается исходом — подтвердить, повторить позже или увести в dead-letter.
 *
 * Заменяет собой прежний `pushLocalChanges`, который просто перебирал pending-записи и не имел
 * ни счётчика попыток, ни backoff, ни способа остановиться на безнадёжной операции.
 *
 * ### Порядок и головная блокировка
 * Записи обрабатываются строго по `sequenceNo` и прогон **останавливается на первой неудаче**.
 * Это осознанный размен: операции связаны между собой (счёт должен появиться на сервере раньше
 * своих транзакций, создание — раньше правки той же сущности), и отправка следующей операции
 * поверх неуехавшей предыдущей приводит к обращению к несуществующему ресурсу. Пропускная
 * способность здесь важнее корректности не является.
 *
 * Терминальные ошибки очередь не блокируют: такая запись сразу уходит в `FAILED` и на следующем
 * прогоне уже не выбирается.
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
class OutboxProcessor @Inject constructor(
    private val localDataSource: OutboxLocalDataSource,
    private val sender: OutboxSender,
    private val retryPolicy: OutboxRetryPolicy,
    private val clock: Clock,
    private val errorLogger: ErrorLogger,
    private val transactionRunner: TransactionRunner
) {

    /**
     * Разбирает очередь.
     *
     * Возвращает `true`, если ни одна запись не осталась неотправленной по временной причине, —
     * то есть повторный прогон прямо сейчас не нужен.
     *
     * ### Почему несколько проходов
     * Выборка берётся один раз на проход, а барьер порядка не выдаёт операцию, пока в её группе
     * есть незакрытая предыдущая. Значит, успешная отправка **разблокирует** следующую операцию
     * своей группы — но та в уже выбранный батч не попала. Без повторного прохода она ждала бы
     * ближайшего фонового синка: пользователь создал транзакцию и тут же её поправил, а правка
     * уехала бы через час.
     *
     * Поэтому проходы повторяются, пока предыдущий что-то закрыл. [MAX_PASSES] — страховка от
     * бесконечного цикла, если какая-то запись начнёт закрываться и появляться снова.
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

    /** Один проход: берёт готовые записи и отправляет их, пропуская застрявшие группы. */
    private suspend fun drainOnce(handled: MutableSet<Long>): PassResult {
        val staleBefore = clock.millis() - LEASE_MILLIS
        val ready = localDataSource.getReadyToSend(
            now = clock.millis(),
            staleBefore = staleBefore,
            limit = BATCH_LIMIT
        ).filterNot { it.sequenceNo in handled }

        // Группы, застрявшие в этом проходе: их последующие операции трогать нельзя, а вот
        // операции других групп от этого не зависят и должны уехать.
        val stalledGroups = mutableSetOf<String>()
        var allDrained = true
        var completed = 0

        for (entry in ready) {
            handled += entry.sequenceNo

            when (claimAndHandle(entry, staleBefore, stalledGroups)) {
                EntryOutcome.DONE -> completed++

                // Пропуск прогрессом не считается: иначе проходы шли бы по кругу
                EntryOutcome.SKIPPED -> Unit

                EntryOutcome.GROUP_STALLED -> {
                    stalledGroups += entry.dependencyKey
                    allDrained = false
                }

                // Сеть заблокирована целиком — остальные упрутся в ту же стену
                EntryOutcome.RUN_STALLED ->
                    return PassResult(completed, allDrained = false, runStalled = true)
            }
        }
        return PassResult(completed, allDrained, runStalled = false)
    }

    /**
     * Пропускает запись, которую брать нельзя, иначе захватывает её и отправляет.
     *
     * Пропуск — это [EntryOutcome.DONE]: для прогона такая запись не событие, он идёт дальше.
     */
    private suspend fun claimAndHandle(
        entry: OutboxEntryEntity,
        staleBefore: Long,
        stalledGroups: Set<String>
    ): EntryOutcome = when {
        // Предыдущая операция группы не уехала — эта пойдёт поверх несуществующего ресурса
        entry.dependencyKey in stalledGroups -> EntryOutcome.SKIPPED

        // Запись уже забрал параллельный прогон — не отправляем её второй раз
        !localDataSource.markInProgress(entry.sequenceNo, staleBefore, clock.millis()) ->
            EntryOutcome.SKIPPED

        else -> handle(entry)
    }

    /** Что делать с прогоном после одной записи. */
    private enum class EntryOutcome {
        /** Запись закрыта (успешно или в dead-letter) — идём дальше. */
        DONE,

        /** Запись не трогали: её группа застряла или её забрал параллельный прогон. */
        SKIPPED,

        /** Операция осталась в очереди: её группу трогать нельзя, другие — можно. */
        GROUP_STALLED,

        /** Отправлять сейчас нельзя вообще ничего. */
        RUN_STALLED
    }

    /** Обрабатывает исход отправки и решает судьбу прогона. */
    private suspend fun handle(entry: OutboxEntryEntity): EntryOutcome {
        return when (val result = sender.send(entry)) {
            is OutboxSendResult.Success -> {
                // Обратный путь атомарен так же, как и прямой: доменная строка подтверждается и
                // операция закрывается одной транзакцией. Иначе смерть процесса между двумя
                // записями оставила бы подтверждённую строку с незакрытой операцией, и та уехала
                // бы на сервер повторно после истечения аренды.
                transactionRunner.runInTransaction {
                    result.localEffect?.apply()
                    localDataSource.markCompleted(entry.sequenceNo, clock.millis())
                }
                EntryOutcome.DONE
            }

            is OutboxSendResult.Blocked -> {
                // Сеть заблокирована намеренно (гость, нет сессии): возвращаем запись в очередь,
                // не тратя попытку. Прогон прекращаем весь — это состояние клиента, а не сервера,
                // и другие группы упрутся в ту же стену.
                requeue(entry, result.reason)
                EntryOutcome.RUN_STALLED
            }

            // Застревает только эта группа: следующие операции той же сущности отправлять
            // поверх неуехавшей нельзя, а независимые — можно и нужно.
            is OutboxSendResult.Transient -> retryOrGiveUp(entry, result.reason)

            is OutboxSendResult.Terminal -> {
                giveUp(entry, result.reason)
                EntryOutcome.DONE
            }
        }
    }

    /** Возвращает запись в очередь без списания попытки — повторим на следующем прогоне. */
    private suspend fun requeue(entry: OutboxEntryEntity, reason: String) {
        localDataSource.scheduleRetry(
            sequenceNo = entry.sequenceNo,
            attemptCount = entry.attemptCount,
            nextAttemptAt = 0,
            lastError = reason,
            updatedAt = clock.millis()
        )
    }

    /** Планирует повтор либо сдаётся; возвращает, застряла группа или запись уже закрыта. */
    private suspend fun retryOrGiveUp(entry: OutboxEntryEntity, reason: String): EntryOutcome {
        val attemptCount = entry.attemptCount + 1

        if (retryPolicy.isExhausted(attemptCount)) {
            giveUp(entry, "Попытки исчерпаны ($attemptCount): $reason")
            return EntryOutcome.DONE
        }

        val now = clock.millis()
        localDataSource.scheduleRetry(
            sequenceNo = entry.sequenceNo,
            attemptCount = attemptCount,
            nextAttemptAt = retryPolicy.nextAttemptAt(now, attemptCount),
            lastError = reason,
            updatedAt = now
        )
        return EntryOutcome.GROUP_STALLED
    }

    /**
     * Уводит запись в dead-letter вместе с зависящими от неё: ретраи прекращены, проблема видна
     * для разбора.
     *
     * Каскад обязателен — операция, чей предшественник не выполнился, обречена: правка по
     * несуществующему ресурсу вернёт `404`, транзакция без своего счёта — `400`. Без каскада
     * каждая из них честно потратила бы все попытки и оказалась бы в dead-letter позже и с
     * невнятной причиной. Ручной повтор возвращает всю группу целиком, поэтому исправить
     * ситуацию по-прежнему можно одним действием.
     */
    private suspend fun giveUp(entry: OutboxEntryEntity, reason: String) {
        errorLogger.recordError(
            "Outbox: ${entry.operation} ${entry.entityType} ${entity(entry)} отклонена — $reason"
        )
        localDataSource.markFailed(
            sequenceNo = entry.sequenceNo,
            dependencyKey = entry.dependencyKey,
            attemptCount = entry.attemptCount + 1,
            lastError = reason,
            updatedAt = clock.millis()
        )
    }

    /** Идентификатор сущности для сообщения об ошибке — без содержимого операции (в нём суммы). */
    private fun entity(entry: OutboxEntryEntity): String = entry.entityLocalId

    companion object {
        /**
         * Предел проходов за один разбор очереди.
         *
         * Проход повторяется, пока предыдущий что-то закрыл: успешная отправка разблокирует
         * следующую операцию своей группы, а та в уже выбранный батч не попала. Предел —
         * страховка от бесконечного цикла; в норме проходов столько, какова самая длинная
         * цепочка операций над одной сущностью.
         */
        const val MAX_PASSES = 10
    }
}
