package soft.divan.financemanager.core.data.source

import retrofit2.Response
import soft.divan.financemanager.core.data.dto.TransactionDto
import soft.divan.financemanager.core.data.dto.TransactionRequestDto
import soft.divan.financemanager.core.data.dto.TransactionResponseCreateDto

interface TransactionRemoteDataSource {
    suspend fun create(request: TransactionRequestDto): Response<TransactionResponseCreateDto>
    suspend fun getByAccountAndPeriod(
        accountId: Int,
        startDate: String? = null,
        endDate: String? = null
    ): Response<List<TransactionDto>>

    suspend fun get(id: Int): Response<TransactionDto>
    suspend fun update(id: Int, transaction: TransactionRequestDto): Response<TransactionDto>
    suspend fun delete(id: Int): Response<Unit>
}
// Revue me>>
