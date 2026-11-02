package soft.divan.financemanager.core.data.sync

/**
 * Syntactic sugar to call [Syncable.syncWith] while omitting the synchronizer argument
 */
interface Synchronizer {
    suspend fun Syncable.sync() = this@sync.syncWith(this@Synchronizer)
}
