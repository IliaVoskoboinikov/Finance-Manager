package soft.divan.financemanager.core.data.sync

import soft.divan.financemanager.core.data.sync.util.Syncable
import soft.divan.financemanager.core.database.entity.TransactionEntity

interface TransactionSyncManager : Syncable {
    suspend fun pullServerData()
    suspend fun pullFromRemoteForAccount(
        accountLocalId: String,
        startDate: String,
        endDate: String
    )

    suspend fun syncCreate(transactionEntity: TransactionEntity)
    suspend fun syncUpdate(transactionEntity: TransactionEntity)
    suspend fun syncDelete(transactionEntity: TransactionEntity)
}
