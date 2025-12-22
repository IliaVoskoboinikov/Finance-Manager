package soft.divan.financemanager.core.data.source

import retrofit2.Response
import soft.divan.financemanager.core.data.dto.TransactionDto
import soft.divan.financemanager.core.data.dto.TransactionRequestDto


interface TransactionRemoteDataSource {
    suspend fun getTransactionsByAccountAndPeriod(
        accountId: Int,
        startDate: String? = null,
        endDate: String? = null
    ): Response<List<TransactionDto>>

    suspend fun createTransaction(request: TransactionRequestDto): Response<TransactionDto>
    suspend fun getTransaction(id: Int): Response<TransactionDto>
    suspend fun updateTransaction(id: Int, transaction: TransactionRequestDto): Response<TransactionDto>
    suspend fun deleteTransaction(id: Int): Response<Unit>
}
