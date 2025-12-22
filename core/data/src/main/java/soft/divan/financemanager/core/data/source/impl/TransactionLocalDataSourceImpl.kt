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
        accountId: String,
        startDate: String,
        endDate: String
    ): Flow<List<TransactionEntity>> {
        return transactionDao.getTransactionsByAccountAndPeriod(accountId, startDate, endDate)
    }

    override suspend fun getByAccountId(accountId: String): List<TransactionEntity> {
        return transactionDao.getByAccountId(accountId)
    }

    override suspend fun saveTransaction(transaction: TransactionEntity) {
        transactionDao.insert(transaction)
    }

    override suspend fun updateTransactionId(createdAt: String, newId: Int) {
        transactionDao.updateTransactionId(createdAt, newId)
    }

    override suspend fun deleteTransaction(transactionId: Int) {
        transactionDao.deleteTransaction(transactionId)
    }

    override suspend fun getTransactionById(transactionId: Int): TransactionEntity? {
        return transactionDao.getTransactionById(transactionId)
    }

    override suspend fun updateTransaction(transaction: TransactionEntity) {
        return transactionDao.updateTransaction(transaction)
    }



}