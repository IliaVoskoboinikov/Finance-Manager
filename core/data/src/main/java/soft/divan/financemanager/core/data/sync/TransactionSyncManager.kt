package soft.divan.financemanager.core.data.sync

/**
 * Синхронизация транзакций **с сервера на устройство**.
 *
 * Обратное направление обеспечивает очередь исходящих операций: репозитории кладут в неё
 * изменения, а `OutboxProcessor` отправляет их с ретраями и dead-letter
 * (см. [docs/outbox.md](../../../../../../../../../../docs/outbox.md)).
 */
interface TransactionSyncManager : Syncable {
    /** Тянет транзакции всех счетов; `false` — pull хотя бы одного счёта не удался. */
    suspend fun pullServerData(): Boolean

    /** Тянет транзакции счёта за период; `false` — pull не удался и его стоит повторить. */
    suspend fun pullFromRemoteForAccount(
        accountLocalId: String,
        startDate: String,
        endDate: String
    ): Boolean
}
