package soft.divan.financemanager.feature.transaction.transaction_impl.domain.repository


import soft.divan.financemanager.core.domain.model.Transaction

interface TransactionRepository {
    suspend fun getTransactions(id: Int): Result<Transaction>
    suspend fun createTransaction(transaction: Transaction): Result<Unit>
    suspend fun updateTransaction(transaction: Transaction): Result<Unit>
    suspend fun deleteTransaction(transactionId: Int): Result<Unit>
}