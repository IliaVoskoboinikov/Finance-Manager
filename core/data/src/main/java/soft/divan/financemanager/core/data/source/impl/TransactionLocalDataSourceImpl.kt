package soft.divan.financemanager.core.data.source.impl

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.core.data.source.TransactionLocalDataSource
import soft.divan.finansemanager.core.database.dao.TransactionDao
import soft.divan.finansemanager.core.database.entity.TransactionEntity
import javax.inject.Inject

class TransactionLocalDataSourceImpl @Inject constructor(
    private val transactionDao: TransactionDao,
) : TransactionLocalDataSource {

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

    override suspend fun createTransaction(transaction: TransactionEntity) {
        transactionDao.insert(transaction)
    }

    override suspend fun deleteTransaction(localId: String) {
        transactionDao.deleteTransaction(localId)
    }

    override suspend fun getTransactionByLocalId(localId: String): TransactionEntity? {
        return transactionDao.getTransactionByLocalId(localId)
    }

    override suspend fun getTransactionByServerId(id: Int): TransactionEntity? {
        return transactionDao.getTransactionByServerId(id)
    }

    override suspend fun updateTransaction(transaction: TransactionEntity) {
        return transactionDao.updateTransaction(transaction)
    }

    override suspend fun getTransactionsByServerIds(serverIds: List<Int>): List<TransactionEntity> {
        if (serverIds.isEmpty()) return emptyList()
        return transactionDao.getTransactionsByServerIds(serverIds)
    }

    override suspend fun getPendingSync(): List<TransactionEntity> = transactionDao.getPendingSync()

}