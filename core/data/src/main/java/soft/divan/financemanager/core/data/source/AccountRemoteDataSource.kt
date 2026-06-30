package soft.divan.financemanager.core.data.source

import retrofit2.Response
import soft.divan.financemanager.core.data.dto.AccountDto
import soft.divan.financemanager.core.data.dto.CreateAccountRequestDto
import soft.divan.financemanager.core.data.dto.UpdateAccountRequestDto

interface AccountRemoteDataSource {
    suspend fun create(request: CreateAccountRequestDto): Response<AccountDto>
    suspend fun getAll(): Response<List<AccountDto>>
    suspend fun getById(id: String): Response<AccountDto>
    suspend fun update(id: String, account: UpdateAccountRequestDto): Response<Unit>
    suspend fun delete(id: String): Response<Unit>
}
