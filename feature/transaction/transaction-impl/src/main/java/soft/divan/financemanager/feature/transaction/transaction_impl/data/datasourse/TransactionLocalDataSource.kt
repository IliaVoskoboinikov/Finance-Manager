package soft.divan.financemanager.feature.transaction.transaction_impl.data.datasourse

import soft.divan.finansemanager.core.database.entity.TransactionEntity


interface TransactionLocalDataSource {
    suspend fun saveTransaction(transaction: TransactionEntity)
    suspend fun updateTransactionId(createdAt: String, newId: Int)

}