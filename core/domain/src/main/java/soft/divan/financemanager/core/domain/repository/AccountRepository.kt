package soft.divan.financemanager.core.domain.repository

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.core.domain.model.Account
import soft.divan.financemanager.core.domain.model.CreateAccountRequest

interface AccountRepository {
    suspend fun getAccounts(): Flow<List<Account>>
    fun createAccount(createAccountRequest: CreateAccountRequest): Result<Unit>
    suspend fun updateAccount(id: Int, account: CreateAccountRequest): Result<Unit>
    suspend fun deleteAccount(id: Int): Result<Unit>
    suspend fun getAccountById(id: Int): Result<Account>

}