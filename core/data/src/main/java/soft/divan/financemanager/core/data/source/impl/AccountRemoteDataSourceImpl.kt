package soft.divan.financemanager.core.data.source.impl

import retrofit2.Response
import soft.divan.financemanager.core.data.api.AccountApiService
import soft.divan.financemanager.core.data.dto.AccountDto
import soft.divan.financemanager.core.data.dto.CreateAccountRequestDto
import soft.divan.financemanager.core.data.dto.UpdateAccountRequestDto
import soft.divan.financemanager.core.data.source.AccountRemoteDataSource
import javax.inject.Inject

class AccountRemoteDataSourceImpl @Inject constructor(
    private val apiService: AccountApiService
) : AccountRemoteDataSource {

    override suspend fun create(request: CreateAccountRequestDto): Response<AccountDto> =
        apiService.createAccount(request)

    override suspend fun getAll(): Response<List<AccountDto>> =
        apiService.getAccounts()

    override suspend fun getById(id: String): Response<AccountDto> =
        apiService.getById(id)

    override suspend fun update(id: String, account: UpdateAccountRequestDto): Response<Unit> =
        apiService.updateAccount(id, account)

    override suspend fun delete(id: String): Response<Unit> =
        apiService.delete(id)
}
