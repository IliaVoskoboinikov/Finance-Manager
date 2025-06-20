package soft.divan.financemanager.data.source

import soft.divan.financemanager.data.network.dto.AccountDto
import soft.divan.financemanager.data.network.dto.CreateAccountRequestDto
import soft.divan.financemanager.domain.utils.Rezult


interface AccountRemoteDataSource {
    suspend fun getAccounts(): Rezult<List<AccountDto>>
    suspend fun createAccount(createAccountRequestDto: CreateAccountRequestDto): Rezult<AccountDto>

}