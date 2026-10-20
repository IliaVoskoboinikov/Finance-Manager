package soft.divan.financemanager.core.data.source

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.core.database.entity.OutboxEntryEntity

/** Доступ к очереди исходящих операций (Transactional Outbox). */
@Suppress("TooManyFunctions") // Очередь: по операции на каждый переход статуса и каждую проверку
interface OutboxLocalDataSource {

    /** Ставит операцию в очередь и возвращает присвоенный `sequenceNo`. */
    suspend fun enqueue(entry: OutboxEntryEntity): Long

    /**
     * Записи, готовые к отправке: отбывшие backoff, а также зависшие в работе — те, чья аренда
     * истекла раньше [staleBefore] (прогон, взявший их, не доложил об исходе).
     */
    suspend fun getReadyToSend(now: Long, staleBefore: Long, limit: Int): List<OutboxEntryEntity>

    /** Захватывает запись в работу; `true` — захват удался (защита от двойной отправки). */
    suspend fun markInProgress(sequenceNo: Long, staleBefore: Long, updatedAt: Long): Boolean

    suspend fun markCompleted(sequenceNo: Long, updatedAt: Long)

    suspend fun scheduleRetry(
        sequenceNo: Long,
        attemptCount: Int,
        nextAttemptAt: Long,
        lastError: String?,
        updatedAt: Long
    )

    /**
     * Уводит запись в dead-letter. Если это создание, вместе с ней уходят зависящие от неё —
     * последующие операции той же строки и те, кто назвал её предшественником: без созданного
     * ресурса они всё равно не выполнятся (см. `OutboxDao`). Результат — сколько записей затронуто.
     */
    suspend fun markFailed(
        sequenceNo: Long,
        entityLocalId: String,
        attemptCount: Int,
        lastError: String?,
        updatedAt: Long
    ): Int

    /** Есть ли у строки незакрытые операции, кроме [exceptSequenceNo] (см. `OutboxDao`). */
    suspend fun hasOpenOperations(entityLocalId: String, exceptSequenceNo: Long): Boolean

    /**
     * Есть ли незакрытые операции над строкой [localId] или над теми, кто от неё зависит:
     * пока они есть, серверная версия строки устарела (см. `OutboxDao`).
     */
    suspend fun hasOpenOperationsAffecting(localId: String): Boolean

    /** Сколько операций осело в dead-letter. */
    fun observeFailedCount(): Flow<Int>

    /** Возвращает записи из dead-letter в очередь; результат — сколько записей вернулось. */
    suspend fun requeueFailed(updatedAt: Long): Int

    suspend fun deleteCompleted()
}
