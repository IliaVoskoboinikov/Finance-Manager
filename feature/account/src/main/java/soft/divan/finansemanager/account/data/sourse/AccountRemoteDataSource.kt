package soft.divan.finansemanager.account.data.sourse

import retrofit2.Response
import soft.divan.financemanager.core.network.dto.AccountDto
import soft.divan.financemanager.core.network.dto.AccountWithStatsDto
import soft.divan.financemanager.core.network.dto.CreateAccountRequestDto
import soft.divan.finansemanager.account.domain.model.AccountBrief


interface AccountRemoteDataSource {
    suspend fun getAccounts(): Response<List<AccountDto>>
    suspend fun createAccount(createAccountRequestDto: CreateAccountRequestDto): Response<AccountDto>
    suspend fun updateAccount(accountBrief: AccountBrief): Response<AccountWithStatsDto>
}