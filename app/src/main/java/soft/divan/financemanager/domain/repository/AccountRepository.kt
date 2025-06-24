package soft.divan.financemanager.domain.repository

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.domain.model.Account
import soft.divan.financemanager.domain.model.AccountBrief
import soft.divan.financemanager.domain.model.CreateAccountRequest

interface AccountRepository {
    fun getAccounts(): Flow<List<Account>>
     fun createAccount(createAccountRequest: CreateAccountRequest): Flow<Account>

     fun updateAccount(accountBrief: AccountBrief): Flow<AccountBrief>


}