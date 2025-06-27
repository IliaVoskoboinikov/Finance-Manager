package soft.divan.financemanager.data.source

import retrofit2.Response
import soft.divan.financemanager.data.network.api.AccountApiService
import soft.divan.financemanager.data.network.api.TransactionApiService
import soft.divan.financemanager.data.network.dto.TransactionDto
import javax.inject.Inject

class TransactionRemoteDataSourceImpl @Inject constructor(
    private val transactionApiService: TransactionApiService,
) : TransactionRemoteDataSource {
    override suspend fun getTransactionsByAccountAndPeriod(
        accountId: Int,
        startDate: String?,
        endDate: String?
    ): Response<List<TransactionDto>> {
        return transactionApiService.getTransactionsByAccountAndPeriod(accountId, startDate, endDate)
    }
}