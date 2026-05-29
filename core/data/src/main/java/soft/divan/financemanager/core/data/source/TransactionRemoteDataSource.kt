package soft.divan.financemanager.core.data.source

import retrofit2.Response
import soft.divan.financemanager.core.data.dto.TransactionDto
import soft.divan.financemanager.core.data.dto.TransactionRequestDto
import soft.divan.financemanager.core.data.dto.UpdateTransactionRequestDto

interface TransactionRemoteDataSource {
    suspend fun create(request: TransactionRequestDto): Response<TransactionDto>
    suspend fun getByAccountAndPeriod(
        accountId: String,
        startDate: String? = null,
        endDate: String? = null
    ): Response<List<TransactionDto>>

    suspend fun get(id: String): Response<TransactionDto>
    suspend fun update(id: String, transaction: UpdateTransactionRequestDto): Response<Unit>
    suspend fun delete(id: String): Response<Unit>
}
