package soft.divan.financemanager.core.domain.repository

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.core.domain.model.Transaction

interface TransactionRepository {
    suspend fun getTransactionsByAccountAndPeriod(
        accountId: String,
        startDate: String,
        endDate: String
    ): Flow<List<Transaction>>

    suspend fun getTransaction(id: Int): Result<Transaction>
    suspend fun createTransaction(transaction: Transaction): Result<Unit>
    suspend fun updateTransaction(transaction: Transaction): Result<Unit>
    suspend fun deleteTransaction(transactionId: Int): Result<Unit>
}