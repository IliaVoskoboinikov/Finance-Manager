package soft.divan.financemanager.feature.transaction.transaction_impl.data.datasourse

import retrofit2.Response
import soft.divan.financemanager.core.data.dto.TransactionDto
import soft.divan.financemanager.feature.transaction.transaction_impl.data.dto.TransactionRequestDto

interface TransactionRemoteDataSource {
    suspend fun createTransaction(request: TransactionRequestDto): Response<TransactionRequestDto>
    suspend fun getTransaction(id: Int): Response<TransactionDto>
    suspend fun updateTransaction(id: Int, request: TransactionRequestDto): Response<TransactionDto>
    suspend fun deleteTransaction(id: Int): Response<Unit>
}
