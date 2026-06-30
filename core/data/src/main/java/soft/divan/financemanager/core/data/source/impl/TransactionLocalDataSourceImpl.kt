package soft.divan.financemanager.core.data.source.impl

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.core.data.source.TransactionLocalDataSource
import soft.divan.financemanager.core.database.dao.TransactionDao
import soft.divan.financemanager.core.database.entity.TransactionEntity
import javax.inject.Inject

class TransactionLocalDataSourceImpl @Inject constructor(
    private val transactionDao: TransactionDao
) : TransactionLocalDataSource {
    override suspend fun insert(transaction: TransactionEntity) = transactionDao.insert(transaction)

    override fun getByAccountAndPeriod(
        accountId: String,
        startDate: String,
        endDate: String
    ): Flow<List<TransactionEntity>> =
        transactionDao.getByAccountAndPeriod(accountId, startDate, endDate)

    override suspend fun getByLocalId(localId: String): TransactionEntity? =
        transactionDao.getByLocalId(localId)

    override suspend fun getByServerId(id: String): TransactionEntity? =
        transactionDao.getByServerId(id)

    override suspend fun getByServerIds(serverIds: List<String>): List<TransactionEntity> =
        transactionDao.getByServerIds(serverIds)

    override suspend fun getByAccountId(accountId: String): List<TransactionEntity> =
        transactionDao.getByAccountId(accountId)

    override suspend fun getPendingSync(): List<TransactionEntity> =
        transactionDao.getPendingSync()

    override suspend fun update(transaction: TransactionEntity) =
        transactionDao.update(transaction)

    override suspend fun delete(localId: String) =
        transactionDao.delete(localId)

    override suspend fun deleteAll() =
        transactionDao.deleteAll()
}
