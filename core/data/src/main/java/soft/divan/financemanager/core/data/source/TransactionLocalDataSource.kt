package soft.divan.financemanager.core.data.source

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.core.database.entity.TransactionEntity

interface TransactionLocalDataSource {
    suspend fun insert(transaction: TransactionEntity)
    fun getByAccountAndPeriod(
        accountId: String,
        startDate: String,
        endDate: String
    ): Flow<List<TransactionEntity>>
    suspend fun getByLocalId(localId: String): TransactionEntity?
    suspend fun getByServerId(id: String): TransactionEntity?
    suspend fun getByServerIds(serverIds: List<String>): List<TransactionEntity>
    suspend fun getByAccountId(accountId: String): List<TransactionEntity>
    suspend fun getPendingSync(): List<TransactionEntity>
    suspend fun update(transaction: TransactionEntity)
    suspend fun delete(localId: String)
    suspend fun deleteAll()
}
