package soft.divan.financemanager.core.data.source.impl

import retrofit2.Response
import soft.divan.financemanager.core.data.api.AccountApiService
import soft.divan.financemanager.core.data.dto.AccountDto
import soft.divan.financemanager.core.data.dto.AccountWithStatsDto
import soft.divan.financemanager.core.data.dto.CreateAccountRequestDto
import soft.divan.financemanager.core.data.dto.UpdateAccountRequestDto
import soft.divan.financemanager.core.data.source.AccountRemoteDataSource
import soft.divan.financemanager.core.domain.model.AccountBrief
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

    override suspend fun updateAccount(accountBrief: AccountBrief): Response<AccountWithStatsDto> {
        val requestDto = UpdateAccountRequestDto(
            name = accountBrief.name,
            balance = accountBrief.balance.toPlainString(),
            currency = accountBrief.currency
        )

        val accountId = accountBrief.id
        return accountApiService.updateAccount(accountId, requestDto)
    }
}