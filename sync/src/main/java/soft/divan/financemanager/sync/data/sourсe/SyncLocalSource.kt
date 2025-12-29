package soft.divan.financemanager.sync.data.sourсe

import kotlinx.coroutines.flow.Flow

interface SyncLocalSource {
    fun observeLastSyncTime(): Flow<Long?>
    suspend fun setLastSyncTime(timeMillis: Long)
    fun observeSyncIntervalHours(): Flow<Int?>
    suspend fun setSyncIntervalHours(hours: Int)
}