package soft.divan.financemanager.core.domain.repository

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.core.domain.model.Account
import soft.divan.financemanager.core.domain.model.AccountBrief

interface AccountRepository {
    suspend fun getAccounts(): Flow<List<Account>>
    fun createAccount(account: AccountBrief): Result<Unit>
    suspend fun updateAccount(account: AccountBrief): Result<Unit>
    suspend fun deleteAccount(id: Int): Result<Unit>
    suspend fun getAccountById(id: Int): Result<Account>

}