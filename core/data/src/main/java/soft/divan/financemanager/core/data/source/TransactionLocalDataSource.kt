package soft.divan.financemanager.core.data.source

import kotlinx.coroutines.flow.Flow
import soft.divan.finansemanager.core.database.entity.TransactionEntity


interface TransactionLocalDataSource {

    suspend fun insertTransactions(transactions: List<TransactionEntity>)

    suspend fun getTransactionsByAccountAndPeriod(
        accountId: String,
        startDate: String,
        endDate: String
    ): Flow<List<TransactionEntity>>

    suspend fun getByAccountId(accountId: String): List<TransactionEntity>
}