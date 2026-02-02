package soft.divan.financemanager.core.data.sync

import soft.divan.finansemanager.core.database.entity.TransactionEntity

interface TransactionSyncManager {
    suspend fun pullServerData()
    suspend fun pullTransactionsFromRemoteForAccount(
        accountLocalId: String,
        startDate: String,
        endDate: String
    )

    suspend fun syncCreate(transactionEntity: TransactionEntity)
    suspend fun syncUpdate(transactionEntity: TransactionEntity)
    suspend fun syncDelete(transactionEntity: TransactionEntity)
}
