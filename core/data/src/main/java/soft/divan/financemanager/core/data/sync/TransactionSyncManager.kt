package soft.divan.financemanager.core.data.sync

import soft.divan.financemanager.core.data.sync.util.Syncable

/**
 * Синхронизация транзакций **с сервера на устройство**.
 *
 * Обратное направление обеспечивает очередь исходящих операций: репозитории кладут в неё
 * изменения, а `OutboxProcessor` отправляет их с ретраями и dead-letter
 * (см. [docs/outbox-plan.md](../../../../../../../../../../docs/outbox-plan.md)).
 */
interface TransactionSyncManager : Syncable {
    suspend fun pullServerData()

    suspend fun pullFromRemoteForAccount(
        accountLocalId: String,
        startDate: String,
        endDate: String
    )
}
