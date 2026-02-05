package soft.divan.financemanager.core.data.sync.util

/**
 * Syntactic sugar to call [Syncable.syncWith] while omitting the synchronizer argument
 */
interface Synchronizer {
    suspend fun Syncable.sync() = this@sync.syncWith(this@Synchronizer)
}

/**
 * Synchronizes the local database backing the repository with the network.
 * Returns if the sync was successful or not.
 */
interface Syncable {
    suspend fun syncWith(synchronizer: Synchronizer): Boolean
}
