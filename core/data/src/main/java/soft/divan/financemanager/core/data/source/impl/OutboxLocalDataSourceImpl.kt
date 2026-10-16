package soft.divan.financemanager.core.data.source.impl

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.core.data.source.OutboxLocalDataSource
import soft.divan.financemanager.core.database.entity.OutboxEntryEntity
import soft.divan.financemanager.core.database.holder.DatabaseHolder
import javax.inject.Inject

class OutboxLocalDataSourceImpl @Inject constructor(
    private val holder: DatabaseHolder
) : OutboxLocalDataSource {

    override suspend fun enqueue(entry: OutboxEntryEntity): Long =
        holder.withDatabase { it.outboxDao().insert(entry) }

    override suspend fun getReadyToSend(
        now: Long,
        staleBefore: Long,
        limit: Int
    ): List<OutboxEntryEntity> =
        holder.withDatabase { it.outboxDao().getReadyToSend(now, staleBefore, limit) }

    /** DAO возвращает число изменённых строк: 0 означает, что запись уже забрал другой прогон. */
    override suspend fun markInProgress(
        sequenceNo: Long,
        staleBefore: Long,
        updatedAt: Long
    ): Boolean = holder.withDatabase {
        it.outboxDao().markInProgress(sequenceNo, staleBefore, updatedAt) > 0
    }

    override suspend fun markCompleted(sequenceNo: Long, updatedAt: Long) =
        holder.withDatabase { it.outboxDao().markCompleted(sequenceNo, updatedAt) }

    override suspend fun scheduleRetry(
        sequenceNo: Long,
        attemptCount: Int,
        nextAttemptAt: Long,
        lastError: String?,
        updatedAt: Long
    ) = holder.withDatabase {
        it.outboxDao().scheduleRetry(sequenceNo, attemptCount, nextAttemptAt, lastError, updatedAt)
    }

    override suspend fun markFailed(
        sequenceNo: Long,
        entityLocalId: String,
        attemptCount: Int,
        lastError: String?,
        updatedAt: Long
    ): Int = holder.withDatabase {
        it.outboxDao().markFailed(
            sequenceNo = sequenceNo,
            entityLocalId = entityLocalId,
            attemptCount = attemptCount,
            lastError = lastError,
            updatedAt = updatedAt
        )
    }

    override fun observeFailedCount(): Flow<Int> =
        holder.observe { it.outboxDao().observeFailedCount() }

    override suspend fun countUnsent(): Int = holder.withDatabase { it.outboxDao().countUnsent() }

    override suspend fun requeueFailed(updatedAt: Long): Int =
        holder.withDatabase { it.outboxDao().requeueFailed(updatedAt) }

    override suspend fun deleteCompleted() = holder.withDatabase { it.outboxDao().deleteCompleted() }
}
