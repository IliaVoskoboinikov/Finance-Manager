package soft.divan.financemanager.domain.repository

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.domain.model.Account
import soft.divan.financemanager.domain.model.AccountBrief
import soft.divan.financemanager.domain.model.CreateAccountRequest
import soft.divan.financemanager.domain.utils.Rezult

interface AccountRepository {
    fun getAccounts(): Flow<List<Account>>
    suspend fun createAccount(createAccountRequest: CreateAccountRequest): Rezult<Account>

    suspend fun updateAccount(accountBrief: AccountBrief): Rezult<AccountBrief>


}