package soft.divan.financemanager.data.source

import retrofit2.Response
import soft.divan.financemanager.data.network.dto.AccountDto
import soft.divan.financemanager.data.network.dto.AccountWithStatsDto
import soft.divan.financemanager.data.network.dto.CreateAccountRequestDto
import soft.divan.financemanager.domain.model.AccountBrief


interface AccountRemoteDataSource {
    suspend fun getAccounts(): Response<List<AccountDto>>
    suspend fun createAccount(createAccountRequestDto: CreateAccountRequestDto): Response<AccountDto>
    suspend fun updateAccount(accountBrief: AccountBrief): Response<AccountWithStatsDto>
}