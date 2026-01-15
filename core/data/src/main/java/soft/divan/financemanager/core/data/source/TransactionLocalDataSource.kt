package soft.divan.financemanager.core.data.source

import kotlinx.coroutines.flow.Flow
import soft.divan.finansemanager.core.database.entity.TransactionEntity


interface TransactionLocalDataSource {
    suspend fun getTransactionsByAccountAndPeriod(
        accountId: String,
        startDate: String,
        endDate: String
    ): Flow<List<TransactionEntity>>

    suspend fun getByAccountId(accountId: String): List<TransactionEntity>
    suspend fun createTransaction(transaction: TransactionEntity)
    suspend fun deleteTransaction(localId: String)
    suspend fun getTransactionByLocalId(localId: String): TransactionEntity?
    suspend fun getTransactionByServerId(id: Int): TransactionEntity?
    suspend fun updateTransaction(transaction: TransactionEntity)
    suspend fun getTransactionsByServerIds(serverIds: List<Int>): List<TransactionEntity>
    suspend fun getPendingSync(): List<TransactionEntity>
}