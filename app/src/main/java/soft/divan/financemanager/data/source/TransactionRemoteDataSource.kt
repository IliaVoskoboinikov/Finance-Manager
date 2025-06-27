package soft.divan.financemanager.data.source

import retrofit2.Response
import soft.divan.financemanager.data.network.dto.TransactionDto

interface TransactionRemoteDataSource {
    suspend fun getTransactionsByAccountAndPeriod(
        accountId: Int,
        startDate: String? = null,
        endDate: String? = null
    ): Response<List<TransactionDto>>
}
