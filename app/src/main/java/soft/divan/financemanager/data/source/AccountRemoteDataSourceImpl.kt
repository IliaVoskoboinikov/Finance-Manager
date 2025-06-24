package soft.divan.financemanager.data.source

import retrofit2.Response
import soft.divan.financemanager.data.network.api.AccountApiService
import soft.divan.financemanager.data.network.dto.AccountDto
import soft.divan.financemanager.data.network.dto.AccountWithStatsDto
import soft.divan.financemanager.data.network.dto.CreateAccountRequestDto
import soft.divan.financemanager.data.network.dto.UpdateAccountRequestDto
import soft.divan.financemanager.domain.model.AccountBrief
import javax.inject.Inject

class AccountRemoteDataSourceImpl @Inject constructor(
    private val accountApiService: AccountApiService,
) : AccountRemoteDataSource {
    override suspend fun getAccounts(): Response<List<AccountDto>> {
        return  accountApiService.getAccounts()
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
        return  accountApiService.updateAccount(accountId, requestDto)
    }
}