package soft.divan.financemanager.domain.repository

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.domain.model.Transaction

interface TransactionRepository {
    suspend fun getTransactionsByAccountAndPeriod(
        accountId: Int,
        startDate: String? = null,
        endDate: String? = null
    ): Flow<List<Transaction>>
}