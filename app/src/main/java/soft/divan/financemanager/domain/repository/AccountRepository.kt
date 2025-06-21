package soft.divan.financemanager.domain.repository

import soft.divan.financemanager.domain.model.Account
import soft.divan.financemanager.domain.model.AccountBrief
import soft.divan.financemanager.domain.model.CreateAccountRequest
import soft.divan.financemanager.domain.utils.Rezult

interface AccountRepository {
    suspend fun getAccounts(): Rezult<List<Account>>
    suspend fun createAccount(createAccountRequest: CreateAccountRequest): Rezult<Account>

    suspend fun updateAccount(accountBrief: AccountBrief): Rezult<AccountBrief>


}