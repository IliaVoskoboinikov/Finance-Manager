package soft.divan.financemanager.core.data.source.impl

import retrofit2.Response
import soft.divan.financemanager.core.data.api.TransactionApiService
import soft.divan.financemanager.core.data.dto.TransactionDto
import soft.divan.financemanager.core.data.dto.TransactionRequestDto
import soft.divan.financemanager.core.data.dto.TransactionResponseCreateDto
import soft.divan.financemanager.core.data.source.TransactionRemoteDataSource
import javax.inject.Inject

class TransactionRemoteDataSourceImpl @Inject constructor(
    private val transactionApiService: TransactionApiService
) : TransactionRemoteDataSource {

    override suspend fun create(request: TransactionRequestDto): Response<TransactionResponseCreateDto> =
        transactionApiService.createTransaction(request)

    override suspend fun getByAccountAndPeriod(
        accountId: Int,
        startDate: String?,
        endDate: String?
    ): Response<List<TransactionDto>> =
        transactionApiService.getTransactionsByAccountAndPeriod(accountId, startDate, endDate)

    override suspend fun get(id: Int): Response<TransactionDto> =
        transactionApiService.getTransaction(id)

    override suspend fun update(id: Int, transaction: TransactionRequestDto): Response<TransactionDto> =
        transactionApiService.updateTransaction(id, transaction)

    override suspend fun delete(id: Int): Response<Unit> =
        transactionApiService.deleteTransaction(id)
}
