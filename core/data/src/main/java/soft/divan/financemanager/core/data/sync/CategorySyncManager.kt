package soft.divan.financemanager.core.data.sync

/** Синхронизация категорий **с сервера на устройство** (категории на клиенте не меняются). */
interface CategorySyncManager : Syncable {
    /** Тянет категории с сервера; `false` — pull не удался и его стоит повторить. */
    suspend fun pullServerData(): Boolean
}
