package soft.divan.financemanager.core.data.source.impl

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.core.data.source.TransactionLocalDataSource
import soft.divan.financemanager.core.database.entity.TransactionEntity
import soft.divan.financemanager.core.database.holder.DatabaseHolder
import javax.inject.Inject

class TransactionLocalDataSourceImpl @Inject constructor(
    private val holder: DatabaseHolder
) : TransactionLocalDataSource {
    override suspend fun insert(transaction: TransactionEntity) =
        holder.withDatabase { it.transactionDao().insert(transaction) }

    override fun getByAccountAndPeriod(
        accountId: String,
        startDate: String,
        endDate: String
    ): Flow<List<TransactionEntity>> =
        holder.observe { it.transactionDao().getByAccountAndPeriod(accountId, startDate, endDate) }

    override suspend fun getByLocalId(localId: String): TransactionEntity? =
        holder.withDatabase { it.transactionDao().getByLocalId(localId) }

    override suspend fun getByServerId(id: String): TransactionEntity? =
        holder.withDatabase { it.transactionDao().getByServerId(id) }

    override suspend fun getBySyncIds(ids: List<String>): List<TransactionEntity> =
        holder.withDatabase { it.transactionDao().getBySyncIds(ids) }

    override suspend fun getByAccountId(accountId: String): List<TransactionEntity> =
        holder.withDatabase { it.transactionDao().getByAccountId(accountId) }

    override suspend fun getPendingSync(): List<TransactionEntity> =
        holder.withDatabase { it.transactionDao().getPendingSync() }

    override suspend fun update(transaction: TransactionEntity) =
        holder.withDatabase { it.transactionDao().update(transaction) }

    override suspend fun delete(localId: String) =
        holder.withDatabase { it.transactionDao().delete(localId) }

    override suspend fun deleteAll() =
        holder.withDatabase { it.transactionDao().deleteAll() }
}
