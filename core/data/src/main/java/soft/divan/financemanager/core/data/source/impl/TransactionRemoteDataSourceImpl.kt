package soft.divan.financemanager.core.data.source.impl

import retrofit2.Response
import soft.divan.financemanager.core.data.api.TransactionApiService
import soft.divan.financemanager.core.data.dto.TransactionDto
import soft.divan.financemanager.core.data.source.TransactionRemoteDataSource
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