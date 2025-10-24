package soft.divan.financemanager.core.data.source.impl

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.core.data.source.TransactionLocalDataSource
import soft.divan.finansemanager.core.database.dao.TransactionDao
import soft.divan.finansemanager.core.database.entity.TransactionEntity
import javax.inject.Inject

class TransactionLocalDataSourceImpl @Inject constructor(
    private val transactionDao: TransactionDao,
) : TransactionLocalDataSource {

    override suspend fun insertTransactions(transactions: List<TransactionEntity>) {
        transactionDao.insertAll(transactions)
    }

    override suspend fun getTransactionsByAccountAndPeriod(
        accountId: Int,
        startDate: String,
        endDate: String
    ): Flow<List<TransactionEntity>> {
        return transactionDao.getTransactionsByAccountAndPeriod(accountId, startDate, endDate)
    }
}