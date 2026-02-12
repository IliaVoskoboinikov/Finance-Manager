package soft.divan.financemanager.sync.worker

interface SyncCoordinator {
    suspend fun syncAll(): Boolean
}
