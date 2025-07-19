package soft.divan.financemanager.feature.transaction.transaction_impl.data.datasourse.impl

import soft.divan.financemanager.feature.transaction.transaction_impl.data.datasourse.TransactionLocalDataSource
import soft.divan.finansemanager.core.database.dao.TransactionDao
import soft.divan.finansemanager.core.database.entity.TransactionEntity
import javax.inject.Inject

class TransactionLocalDataSourceImpl @Inject constructor(
    val transactionDto: TransactionDao

) : TransactionLocalDataSource {
    override suspend fun saveTransaction(transaction: TransactionEntity) {
        transactionDto.insert(transaction)
    }

}