package soft.divan.financemanager.core.data.source.impl

import retrofit2.Response
import soft.divan.financemanager.core.data.api.TransactionApiService
import soft.divan.financemanager.core.data.dto.TransactionDto
import soft.divan.financemanager.core.data.dto.TransactionRequestDto
import soft.divan.financemanager.core.data.dto.TransactionResponseCreateDto
import soft.divan.financemanager.core.data.source.TransactionRemoteDataSource
import javax.inject.Inject

class TransactionRemoteDataSourceImpl @Inject constructor(
    private val apiService: TransactionApiService
) : TransactionRemoteDataSource {

    override suspend fun create(request: TransactionRequestDto): Response<TransactionResponseCreateDto> =
        apiService.createTransaction(request)

    override suspend fun getByAccountAndPeriod(
        accountId: String,
        startDate: String?,
        endDate: String?
    ): Response<List<TransactionDto>> =
        apiService.getTransactionsByAccountAndPeriod(accountId, startDate, endDate)

    override suspend fun get(id: String): Response<TransactionDto> =
        apiService.getTransaction(id)

    override suspend fun update(
        id: String,
        transaction: TransactionRequestDto
    ): Response<TransactionDto> =
        apiService.updateTransaction(id, transaction)

    override suspend fun delete(id: String): Response<Unit> =
        apiService.deleteTransaction(id)
}
