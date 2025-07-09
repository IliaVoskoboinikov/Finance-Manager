package soft.divan.finansemanager.account.domain.repository

import kotlinx.coroutines.flow.Flow
import soft.divan.finansemanager.account.domain.model.Account
import soft.divan.finansemanager.account.domain.model.AccountBrief
import soft.divan.finansemanager.account.domain.model.CreateAccountRequest

interface AccountRepository {
    fun getAccounts(): Flow<List<Account>>
    fun createAccount(createAccountRequest: CreateAccountRequest): Flow<Account>

    fun updateAccount(accountBrief: AccountBrief): Flow<AccountBrief>


}