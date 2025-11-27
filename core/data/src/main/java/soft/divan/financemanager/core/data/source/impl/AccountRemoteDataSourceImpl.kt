package soft.divan.financemanager.core.data.source.impl

import retrofit2.Response
import soft.divan.financemanager.core.data.api.AccountApiService
import soft.divan.financemanager.core.data.dto.AccountDto
import soft.divan.financemanager.core.data.dto.AccountWithStatsDto
import soft.divan.financemanager.core.data.dto.CreateAccountRequestDto
import soft.divan.financemanager.core.data.dto.UpdateAccountRequestDto
import soft.divan.financemanager.core.data.source.AccountRemoteDataSource
import javax.inject.Inject

class AccountRemoteDataSourceImpl @Inject constructor(
    private val accountApiService: AccountApiService,
) : AccountRemoteDataSource {

    override suspend fun getAccounts(): Response<List<AccountDto>> {
        return accountApiService.getAccounts()
    }

    override suspend fun createAccount(createAccountRequestDto: CreateAccountRequestDto): Response<AccountDto> {
        return accountApiService.createAccount(createAccountRequestDto)
    }

    override suspend fun updateAccount(
        id: Int,
        account: UpdateAccountRequestDto
    ): Response<AccountDto> {
        return accountApiService.updateAccount(id, account)
    }

    override suspend fun delete(id: Int): Response<Unit> {
        return accountApiService.delete(id)
    }

    override suspend fun getById(id: Int): Response<AccountWithStatsDto> {
        return accountApiService.getById(id)
    }


}