package soft.divan.financemanager.core.data.source

import kotlinx.coroutines.flow.Flow
import soft.divan.finansemanager.core.database.entity.TransactionEntity

interface TransactionLocalDataSource {
    suspend fun create(transaction: TransactionEntity)
    suspend fun getByAccountAndPeriod(
        accountId: String,
        startDate: String,
        endDate: String
    ): Flow<List<TransactionEntity>>

    suspend fun getByLocalId(localId: String): TransactionEntity?
    suspend fun getByServerId(id: Int): TransactionEntity?
    suspend fun getByAccountId(accountId: String): List<TransactionEntity>
    suspend fun getByServerIds(serverIds: List<Int>): List<TransactionEntity>
    suspend fun getPendingSync(): List<TransactionEntity>
    suspend fun update(transaction: TransactionEntity)
    suspend fun delete(localId: String)
}
