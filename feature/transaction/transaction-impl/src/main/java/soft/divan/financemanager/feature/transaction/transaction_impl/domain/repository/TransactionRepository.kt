package soft.divan.financemanager.feature.transaction.transaction_impl.domain.repository


import soft.divan.financemanager.core.domain.model.Transaction

interface TransactionRepository {
    suspend fun getTransactions(id: String): Result<List<Transaction>>
    suspend fun createTransaction(transaction: Transaction): Result<Transaction>
}