package soft.divan.financemanager.core.data.source.impl

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.core.data.source.TransactionLocalDataSource
import soft.divan.finansemanager.core.database.dao.TransactionDao
import soft.divan.finansemanager.core.database.entity.TransactionEntity
import javax.inject.Inject

class TransactionLocalDataSourceImpl @Inject constructor(
    private val transactionDao: TransactionDao
) : TransactionLocalDataSource {

    override suspend fun create(transaction: TransactionEntity) =
        transactionDao.insert(transaction)

    override suspend fun getByAccountAndPeriod(
        accountId: String,
        startDate: String,
        endDate: String
    ): Flow<List<TransactionEntity>> =
        transactionDao.getByAccountAndPeriod(accountId, startDate, endDate)

    override suspend fun getByAccountId(accountId: String): List<TransactionEntity> =
        transactionDao.getByAccountId(accountId)

    override suspend fun getByLocalId(localId: String): TransactionEntity? =
        transactionDao.getByLocalId(localId)

    override suspend fun getByServerId(id: Int): TransactionEntity? =
        transactionDao.getByServerId(id)

    override suspend fun getByServerIds(serverIds: List<Int>): List<TransactionEntity> =
        if (serverIds.isEmpty()) emptyList() else transactionDao.getByServerIds(serverIds)

    override suspend fun getPendingSync(): List<TransactionEntity> = transactionDao.getPendingSync()

    override suspend fun update(transaction: TransactionEntity) = transactionDao.update(transaction)

    override suspend fun delete(localId: String) = transactionDao.delete(localId)
}
