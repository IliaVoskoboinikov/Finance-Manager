package soft.divan.financemanager.core.data.source

import retrofit2.Response
import soft.divan.financemanager.core.domain.model.AccountBrief
import soft.divan.financemanager.core.network.dto.AccountDto
import soft.divan.financemanager.core.network.dto.AccountWithStatsDto
import soft.divan.financemanager.core.network.dto.CreateAccountRequestDto


interface AccountRemoteDataSource {
    suspend fun getAccounts(): Response<List<AccountDto>>
    suspend fun createAccount(createAccountRequestDto: CreateAccountRequestDto): Response<AccountDto>
    suspend fun updateAccount(accountBrief: AccountBrief): Response<AccountWithStatsDto>
}