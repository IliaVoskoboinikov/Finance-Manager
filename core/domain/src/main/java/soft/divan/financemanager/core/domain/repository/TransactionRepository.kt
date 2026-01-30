package soft.divan.financemanager.core.domain.repository

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.core.domain.model.Transaction
import soft.divan.financemanager.core.domain.result.DomainResult
import java.time.Instant

interface TransactionRepository {
    fun getTransactionsByAccountAndPeriod(
        accountId: String,
        startDate: Instant,
        endDate: Instant
    ): Flow<DomainResult<List<Transaction>>>

    suspend fun getTransactionById(localId: String): DomainResult<Transaction>
    suspend fun createTransaction(transaction: Transaction): DomainResult<Unit>
    suspend fun updateTransaction(transaction: Transaction): DomainResult<Unit>
    suspend fun deleteTransaction(id: String): DomainResult<Unit>
}
