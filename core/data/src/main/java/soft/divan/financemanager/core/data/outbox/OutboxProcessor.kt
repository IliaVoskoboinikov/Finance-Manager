package soft.divan.financemanager.core.data.outbox

import soft.divan.financemanager.core.data.source.OutboxLocalDataSource
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
    private val errorLogger: ErrorLogger
) {

    /**
     * Выполняет один проход по очереди.
     *
     * Возвращает `true`, если ни одна запись не осталась неотправленной по временной причине, —
     * то есть повторный прогон прямо сейчас не нужен.
     */
    suspend fun process(): Boolean {
        val staleBefore = clock.millis() - LEASE_MILLIS
        val ready = localDataSource.getReadyToSend(
            now = clock.millis(),
            staleBefore = staleBefore,
            limit = BATCH_LIMIT
        )

        try {
            for (entry in ready) {
                if (!localDataSource.markInProgress(entry.sequenceNo, staleBefore, clock.millis())) {
                    // Запись уже забрал параллельный прогон — не отправляем её второй раз
                    continue
                }

                if (!handle(entry)) return false
            }
            return true
        } finally {
            // Чистим и при досрочной остановке: иначе уже отправленные записи копились бы в
            // очереди до ближайшего полностью успешного прогона.
            localDataSource.deleteCompleted()
        }
    }

    /** Обрабатывает исход отправки. Возвращает `false`, если прогон дальше идти не должен. */
    private suspend fun handle(entry: OutboxEntryEntity): Boolean {
        return when (val result = sender.send(entry)) {
            is OutboxSendResult.Success -> {
                localDataSource.markCompleted(entry.sequenceNo, clock.millis())
                true
            }

            is OutboxSendResult.Blocked -> {
                // Сеть заблокирована намеренно: возвращаем запись в очередь, не тратя попытку,
                // и прекращаем прогон — остальные упрутся в ту же стену.
                requeue(entry, result.reason)
                false
            }

            is OutboxSendResult.Transient -> {
                retryOrGiveUp(entry, result.reason)
                false
            }

            is OutboxSendResult.Terminal -> {
                giveUp(entry, result.reason)
                true
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

    private suspend fun retryOrGiveUp(entry: OutboxEntryEntity, reason: String) {
        val attemptCount = entry.attemptCount + 1

        if (retryPolicy.isExhausted(attemptCount)) {
            giveUp(entry, "Попытки исчерпаны ($attemptCount): $reason")
            return
        }

        val now = clock.millis()
        localDataSource.scheduleRetry(
            sequenceNo = entry.sequenceNo,
            attemptCount = attemptCount,
            nextAttemptAt = retryPolicy.nextAttemptAt(now, attemptCount),
            lastError = reason,
            updatedAt = now
        )
    }

    /** Уводит запись в dead-letter: ретраи прекращены, проблема видна для разбора. */
    private suspend fun giveUp(entry: OutboxEntryEntity, reason: String) {
        errorLogger.recordError(
            "Outbox: ${entry.operation} ${entry.entityType} ${entity(entry)} отклонена — $reason"
        )
        localDataSource.markFailed(
            sequenceNo = entry.sequenceNo,
            attemptCount = entry.attemptCount + 1,
            lastError = reason,
            updatedAt = clock.millis()
        )
    }

    /** Идентификатор сущности для сообщения об ошибке — без содержимого операции (в нём суммы). */
    private fun entity(entry: OutboxEntryEntity): String = entry.entityLocalId
}
