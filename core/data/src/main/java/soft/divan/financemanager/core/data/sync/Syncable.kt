package soft.divan.financemanager.core.data.sync

/**
 * Synchronizes the local database backing the repository with the network.
 * Returns if the sync was successful or not.
 */
interface Syncable {
    suspend fun syncWith(synchronizer: Synchronizer): Boolean
}
