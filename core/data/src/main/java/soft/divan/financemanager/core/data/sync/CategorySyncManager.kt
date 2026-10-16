package soft.divan.financemanager.core.data.sync

interface CategorySyncManager : Syncable {
    suspend fun pullServerData()
}
