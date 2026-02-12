package soft.divan.financemanager.core.data.sync

import soft.divan.financemanager.core.data.sync.util.Syncable

interface CategorySyncManager : Syncable {
    suspend fun pullServerData()
}
