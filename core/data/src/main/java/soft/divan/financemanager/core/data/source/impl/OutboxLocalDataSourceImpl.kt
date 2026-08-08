package soft.divan.financemanager.core.data.source.impl

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.core.data.source.OutboxLocalDataSource
import soft.divan.financemanager.core.database.dao.OutboxDao
import soft.divan.financemanager.core.database.entity.OutboxEntryEntity
import javax.inject.Inject

class OutboxLocalDataSourceImpl @Inject constructor(
    private val outboxDao: OutboxDao
) : OutboxLocalDataSource {

    override suspend fun enqueue(entry: OutboxEntryEntity): Long = outboxDao.insert(entry)

    override suspend fun getReadyToSend(now: Long, limit: Int): List<OutboxEntryEntity> =
        outboxDao.getReadyToSend(now, limit)

    /** DAO возвращает число изменённых строк: 0 означает, что запись уже забрал другой прогон. */
    override suspend fun markInProgress(sequenceNo: Long, updatedAt: Long): Boolean =
        outboxDao.markInProgress(sequenceNo, updatedAt) > 0

    override suspend fun markCompleted(sequenceNo: Long, updatedAt: Long) =
        outboxDao.markCompleted(sequenceNo, updatedAt)

    override suspend fun scheduleRetry(
        sequenceNo: Long,
        attemptCount: Int,
        nextAttemptAt: Long,
        lastError: String?,
        updatedAt: Long
    ) = outboxDao.scheduleRetry(sequenceNo, attemptCount, nextAttemptAt, lastError, updatedAt)

    override suspend fun markFailed(
        sequenceNo: Long,
        attemptCount: Int,
        lastError: String?,
        updatedAt: Long
    ) = outboxDao.markFailed(sequenceNo, attemptCount, lastError, updatedAt)

    override fun observeFailedCount(): Flow<Int> = outboxDao.observeFailedCount()

    override suspend fun requeueFailed(updatedAt: Long): Int = outboxDao.requeueFailed(updatedAt)

    override suspend fun deleteCompleted() = outboxDao.deleteCompleted()
}
