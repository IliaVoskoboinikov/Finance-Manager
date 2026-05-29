package soft.divan.financemanager.core.data.source

import retrofit2.Response
import soft.divan.financemanager.core.data.dto.TransactionDto
import soft.divan.financemanager.core.data.dto.TransactionRequestDto
import soft.divan.financemanager.core.data.dto.TransactionResponseCreateDto

interface TransactionRemoteDataSource {
    suspend fun create(request: TransactionRequestDto): Response<TransactionResponseCreateDto>
    suspend fun getByAccountAndPeriod(
        accountId: String,
        startDate: String? = null,
        endDate: String? = null
    ): Response<List<TransactionDto>>

    suspend fun get(id: String): Response<TransactionDto>
    suspend fun update(id: String, transaction: TransactionRequestDto): Response<TransactionDto>
    suspend fun delete(id: String): Response<Unit>
}
