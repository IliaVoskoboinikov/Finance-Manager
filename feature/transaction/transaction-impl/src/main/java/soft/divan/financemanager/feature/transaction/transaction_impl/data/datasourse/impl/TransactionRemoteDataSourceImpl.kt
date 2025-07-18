package soft.divan.financemanager.feature.transaction.transaction_impl.data.datasourse.impl

import retrofit2.Response
import soft.divan.financemanager.core.data.dto.TransactionDto
import soft.divan.financemanager.feature.transaction.transaction_impl.data.api.TransactionApiService
import soft.divan.financemanager.feature.transaction.transaction_impl.data.datasourse.TransactionRemoteDataSource
import soft.divan.financemanager.feature.transaction.transaction_impl.data.dto.TransactionRequestDto
import javax.inject.Inject

class TransactionRemoteDataSourceImpl @Inject constructor(
    private val api: TransactionApiService
) : TransactionRemoteDataSource {

    override suspend fun createTransaction(request: TransactionRequestDto): Response<TransactionDto> {
        return api.createTransaction(request)
    }

    override suspend fun getTransaction(id: Int): Response<TransactionDto> {
        return api.getTransaction(id)
    }

    override suspend fun updateTransaction(
        id: Int,
        request: TransactionRequestDto
    ): Response<TransactionDto> {
        return api.updateTransaction(id, request)
    }

    override suspend fun deleteTransaction(id: Int): Response<Unit> {
        return api.deleteTransaction(id)
    }
}
