package soft.divan.financemanager.core.data.source.impl

import retrofit2.Response
import soft.divan.financemanager.core.data.api.TransactionApiService
import soft.divan.financemanager.core.data.dto.TransactionDto
import soft.divan.financemanager.core.data.dto.TransactionRequestDto
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

    override suspend fun createTransaction(request: TransactionRequestDto): Response<TransactionDto> {
        return transactionApiService.createTransaction(request)
    }

    override suspend fun getTransaction(id: Int): Response<TransactionDto> {
        return transactionApiService.getTransaction(id)
    }

    override suspend fun updateTransaction(
        id: Int,
        transaction: TransactionRequestDto
    ): Response<TransactionDto> {
        return transactionApiService.updateTransaction(id, transaction)
    }

    override suspend fun deleteTransaction(id: Int): Response<Unit> {
        return transactionApiService.deleteTransaction(id)
    }
}