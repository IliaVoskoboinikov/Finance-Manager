package soft.divan.financemanager.sync.data.repository

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.sync.data.source.SyncLocalSource
import soft.divan.financemanager.sync.domain.repository.SyncRepository
import javax.inject.Inject

class SyncRepositoryImpl @Inject constructor(
    private val syncLocalSource: SyncLocalSource
) : SyncRepository {

    override fun observeLastSyncTime(): Flow<Long?> {
        return syncLocalSource.observeLastSyncTime()
    }

    override suspend fun setLastSyncTime(timeMillis: Long) {
        syncLocalSource.setLastSyncTime(timeMillis)
    }

    override fun observeSyncIntervalHours(): Flow<Int?> {
        return syncLocalSource.observeSyncIntervalHours()
    }

    override suspend fun setSyncIntervalHours(hours: Int) {
        syncLocalSource.setSyncIntervalHours(hours)
    }
}
