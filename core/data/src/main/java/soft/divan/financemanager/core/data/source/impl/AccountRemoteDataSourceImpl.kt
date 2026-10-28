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

    override suspend fun create(
        request: CreateAccountRequestDto,
        idempotencyKey: String
    ): Response<AccountDto> =
        apiService.createAccount(request, idempotencyKey)

    override suspend fun getAll(): Response<List<AccountDto>> =
        apiService.getAccounts()

    override suspend fun getById(id: String): Response<AccountDto> =
        apiService.getById(id)

    override suspend fun update(
        id: String,
        account: UpdateAccountRequestDto,
        idempotencyKey: String
    ): Response<Unit> =
        apiService.updateAccount(id, account, idempotencyKey)

    override suspend fun delete(id: String, idempotencyKey: String): Response<Unit> =
        apiService.delete(id, idempotencyKey)
}
