package soft.divan.financemanager.sync.domain.repository

import kotlinx.coroutines.flow.Flow

interface SyncRepository {
    fun observeLastSyncTime(): Flow<Long?>
    suspend fun setLastSyncTime(timeMillis: Long)
    fun observeSyncIntervalHours(): Flow<Int?>
    suspend fun setSyncIntervalHours(hours: Int)
}