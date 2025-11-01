package soft.divan.financemanager.feature.transaction.transaction_impl.data.datasourse

import soft.divan.finansemanager.core.database.entity.TransactionEntity


interface TransactionLocalDataSource {

    suspend fun saveTransaction(transaction: TransactionEntity)

    suspend fun updateTransactionId(createdAt: String, newId: Int)

    suspend fun deleteTransaction(transactionId: Int)

    suspend fun getTransactionById(transactionId: Int): TransactionEntity?

    suspend fun updateTransaction(transaction: TransactionEntity)

}