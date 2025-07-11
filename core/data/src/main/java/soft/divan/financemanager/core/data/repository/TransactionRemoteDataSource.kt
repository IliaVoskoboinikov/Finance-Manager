package soft.divan.financemanager.core.data.repository

import retrofit2.Response
import soft.divan.financemanager.core.network.dto.TransactionDto


interface TransactionRemoteDataSource {
    suspend fun getTransactionsByAccountAndPeriod(
        accountId: Int,
        startDate: String? = null,
        endDate: String? = null
    ): Response<List<TransactionDto>>
}
