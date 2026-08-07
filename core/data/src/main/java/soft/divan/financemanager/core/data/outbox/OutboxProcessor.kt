package soft.divan.financemanager.core.data.outbox

import soft.divan.financemanager.core.data.source.OutboxLocalDataSource
import soft.divan.financemanager.core.database.entity.OutboxEntryEntity
import soft.divan.financemanager.core.loggingerror.ErrorLogger
import java.time.Clock
import javax.inject.Inject

/** Сколько записей берём за один прогон, чтобы не занимать сеть надолго. */
private const val BATCH_LIMIT = 50

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
 * ### Устойчивость к повторному запуску
 * Прогон может стартовать одновременно из WorkManager и по факту новой операции, поэтому каждая
 * запись перед отправкой захватывается атомарно; проигравший захват её пропускает.
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
        val ready = localDataSource.getReadyToSend(now = clock.millis(), limit = BATCH_LIMIT)

        for (entry in ready) {
            if (!localDataSource.markInProgress(entry.sequenceNo, clock.millis())) {
                // Запись уже забрал параллельный прогон — не отправляем её второй раз
                continue
            }

            if (!handle(entry)) return false
        }

        localDataSource.deleteCompleted()
        return true
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
