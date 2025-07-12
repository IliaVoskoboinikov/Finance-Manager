package soft.divan.financemanager.core.data.source

import retrofit2.Response
import soft.divan.financemanager.core.data.dto.AccountDto
import soft.divan.financemanager.core.data.dto.AccountWithStatsDto
import soft.divan.financemanager.core.data.dto.CreateAccountRequestDto
import soft.divan.financemanager.core.domain.model.AccountBrief


interface AccountRemoteDataSource {
    suspend fun getAccounts(): Response<List<AccountDto>>
    suspend fun createAccount(createAccountRequestDto: CreateAccountRequestDto): Response<AccountDto>
    suspend fun updateAccount(accountBrief: AccountBrief): Response<AccountWithStatsDto>
}