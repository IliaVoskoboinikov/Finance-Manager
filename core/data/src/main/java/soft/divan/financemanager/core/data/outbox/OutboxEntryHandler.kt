package soft.divan.financemanager.core.data.outbox

import soft.divan.financemanager.core.data.source.OutboxLocalDataSource
import soft.divan.financemanager.core.data.transaction.TransactionRunner
import soft.divan.financemanager.core.database.entity.OutboxEntryEntity
import soft.divan.financemanager.core.loggingerror.ErrorLogger
import java.time.Clock
import javax.inject.Inject

/**
 * Отправляет одну запись очереди и распоряжается исходом: подтвердить, повторить позже или увести
 * в dead-letter.
 *
 * Выделен из [OutboxProcessor], чтобы разделить два независимых вопроса. Процессор отвечает за
 * «какие записи брать, в каком порядке и когда остановиться»; этот класс — за «что делать с одной
 * записью, когда сервер ответил». Их можно менять и проверять порознь: политика повторов не знает
 * про батчи и аренду, а порядок выборки — про коды ответов.
 */
internal class OutboxEntryHandler @Inject constructor(
    private val localDataSource: OutboxLocalDataSource,
    private val sender: OutboxSender,
    private val retryPolicy: OutboxRetryPolicy,
    private val clock: Clock,
    private val errorLogger: ErrorLogger,
    private val transactionRunner: TransactionRunner
) {

    /** Отправляет запись (она уже захвачена процессором) и применяет исход. */
    suspend fun handle(entry: OutboxEntryEntity): OutboxEntryOutcome =
        when (val result = sender.send(entry)) {
            is OutboxSendResult.Success -> {
                // Обратный путь атомарен так же, как и прямой: доменная строка подтверждается и
                // операция закрывается одной транзакцией. Иначе смерть процесса между двумя
                // записями оставила бы подтверждённую строку с незакрытой операцией, и та уехала
                // бы на сервер повторно после истечения аренды.
                transactionRunner.runInTransaction {
                    result.localEffect?.apply()
                    localDataSource.markCompleted(entry.sequenceNo, clock.millis())
                }
                OutboxEntryOutcome.DONE
            }

            is OutboxSendResult.Blocked -> {
                // Сеть заблокирована намеренно (гость, нет сессии): возвращаем запись в очередь,
                // не тратя попытку. Прогон прекращаем весь — это состояние клиента, а не сервера,
                // и остальные операции упрутся в ту же стену.
                requeue(entry, result.reason)
                OutboxEntryOutcome.RUN_STALLED
            }

            is OutboxSendResult.Transient -> retryOrGiveUp(entry, result.reason)

            is OutboxSendResult.Terminal -> {
                giveUp(entry, result.reason)
                OutboxEntryOutcome.DONE
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

    /** Планирует повтор либо сдаётся; возвращает, застряла ли операция или уже закрыта. */
    private suspend fun retryOrGiveUp(
        entry: OutboxEntryEntity,
        reason: String
    ): OutboxEntryOutcome {
        val attemptCount = entry.attemptCount + 1

        if (retryPolicy.isExhausted(attemptCount)) {
            giveUp(entry, "Попытки исчерпаны ($attemptCount): $reason")
            return OutboxEntryOutcome.DONE
        }

        val now = clock.millis()
        localDataSource.scheduleRetry(
            sequenceNo = entry.sequenceNo,
            attemptCount = attemptCount,
            nextAttemptAt = retryPolicy.nextAttemptAt(now, attemptCount),
            lastError = reason,
            updatedAt = now
        )
        // Всё, что ждёт эту операцию, отправлять поверх неуехавшей нельзя; остальное — можно
        return OutboxEntryOutcome.STALLED
    }

    /**
     * Уводит запись в dead-letter вместе с зависящими от неё: ретраи прекращены, проблема видна
     * для разбора.
     *
     * Каскад обязателен — операция, чей предшественник не выполнился, обречена: правка по
     * несуществующему ресурсу вернёт `404`, транзакция без своего счёта — `400`. Без каскада
     * каждая честно потратила бы все попытки и оказалась бы в dead-letter позже и с невнятной
     * причиной. Ручной повтор возвращает их вместе с предшественником, поэтому исправить
     * ситуацию по-прежнему можно одним действием.
     */
    private suspend fun giveUp(entry: OutboxEntryEntity, reason: String) {
        // В сообщение идёт только идентификатор — в содержимом операции лежат суммы
        errorLogger.recordError(
            "Outbox: ${entry.operation} ${entry.entityType} ${entry.entityLocalId} " +
                "отклонена — $reason"
        )
        localDataSource.markFailed(
            sequenceNo = entry.sequenceNo,
            entityLocalId = entry.entityLocalId,
            attemptCount = entry.attemptCount + 1,
            lastError = reason,
            updatedAt = clock.millis()
        )
    }
}
