package soft.divan.financemanager.core.data.sync

/**
 * Синхронизация счетов **с сервера на устройство**.
 *
 * Обратное направление обеспечивает очередь исходящих операций: репозитории кладут в неё
 * изменения, а `OutboxProcessor` отправляет их с ретраями и dead-letter
 * (см. [docs/outbox.md](../../../../../../../../../../docs/outbox.md)).
 */
interface AccountSyncManager : Syncable {
    suspend fun pullServerData()
}
