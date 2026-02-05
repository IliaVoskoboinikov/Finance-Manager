package soft.divan.financemanager.core.data.source

import retrofit2.Response
import soft.divan.financemanager.core.data.dto.AccountDto
import soft.divan.financemanager.core.data.dto.AccountWithStatsDto
import soft.divan.financemanager.core.data.dto.CreateAccountRequestDto

interface AccountRemoteDataSource {
    suspend fun create(createAccountRequestDto: CreateAccountRequestDto): Response<AccountDto>
    suspend fun getAll(): Response<List<AccountDto>>
    suspend fun getById(id: Int): Response<AccountWithStatsDto>
    suspend fun update(id: Int, account: CreateAccountRequestDto): Response<AccountDto>
    suspend fun delete(id: Int): Response<Unit>
}
