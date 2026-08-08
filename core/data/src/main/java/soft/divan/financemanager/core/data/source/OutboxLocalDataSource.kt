package soft.divan.financemanager.core.data.source

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.core.database.entity.OutboxEntryEntity

/** Доступ к очереди исходящих операций (Transactional Outbox). */
interface OutboxLocalDataSource {

    /** Ставит операцию в очередь и возвращает присвоенный `sequenceNo`. */
    suspend fun enqueue(entry: OutboxEntryEntity): Long

    /** Записи, готовые к отправке: ждут своей очереди и уже отбыли backoff. */
    suspend fun getReadyToSend(now: Long, limit: Int): List<OutboxEntryEntity>

    /** Захватывает запись в работу; `true` — захват удался (защита от двойной отправки). */
    suspend fun markInProgress(sequenceNo: Long, updatedAt: Long): Boolean

    suspend fun markCompleted(sequenceNo: Long, updatedAt: Long)

    suspend fun scheduleRetry(
        sequenceNo: Long,
        attemptCount: Int,
        nextAttemptAt: Long,
        lastError: String?,
        updatedAt: Long
    )

    suspend fun markFailed(sequenceNo: Long, attemptCount: Int, lastError: String?, updatedAt: Long)

    /** Сколько операций осело в dead-letter. */
    fun observeFailedCount(): Flow<Int>

    /** Возвращает записи из dead-letter в очередь; результат — сколько записей вернулось. */
    suspend fun requeueFailed(updatedAt: Long): Int

    suspend fun deleteCompleted()
}
