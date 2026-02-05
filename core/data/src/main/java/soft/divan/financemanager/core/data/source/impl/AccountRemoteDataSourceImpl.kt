package soft.divan.financemanager.core.data.source.impl

import retrofit2.Response
import soft.divan.financemanager.core.data.api.AccountApiService
import soft.divan.financemanager.core.data.dto.AccountDto
import soft.divan.financemanager.core.data.dto.AccountWithStatsDto
import soft.divan.financemanager.core.data.dto.CreateAccountRequestDto
import soft.divan.financemanager.core.data.source.AccountRemoteDataSource
import javax.inject.Inject

class AccountRemoteDataSourceImpl @Inject constructor(
    private val accountApiService: AccountApiService
) : AccountRemoteDataSource {

    override suspend fun create(createAccountRequestDto: CreateAccountRequestDto): Response<AccountDto> =
        accountApiService.createAccount(createAccountRequestDto)

    override suspend fun getAll(): Response<List<AccountDto>> = accountApiService.getAccounts()

    override suspend fun getById(id: Int): Response<AccountWithStatsDto> =
        accountApiService.getById(id)

    override suspend fun update(id: Int, account: CreateAccountRequestDto): Response<AccountDto> =
        accountApiService.updateAccount(id, account)

    override suspend fun delete(id: Int): Response<Unit> = accountApiService.delete(id)
}
