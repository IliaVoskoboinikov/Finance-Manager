package soft.divan.financemanager.core.data.sync

/**
 * Синхронизация транзакций **с сервера на устройство**.
 *
 * Обратное направление обеспечивает очередь исходящих операций: репозитории кладут в неё
 * изменения, а `OutboxProcessor` отправляет их с ретраями и dead-letter
 * (см. [docs/outbox.md](../../../../../../../../../../docs/outbox.md)).
 */
interface TransactionSyncManager : Syncable {
    suspend fun pullServerData()

    suspend fun pullFromRemoteForAccount(
        accountLocalId: String,
        startDate: String,
        endDate: String
    )
}
