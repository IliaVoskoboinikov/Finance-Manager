package soft.divan.financemanager.core.domain.repository

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.core.domain.model.Account

interface AccountRepository {
    suspend fun getAccounts(): Flow<List<Account>>
    suspend fun createAccount(account: Account): Result<Unit>
    suspend fun updateAccount(account: Account): Result<Unit>
    suspend fun deleteAccount(id: String): Result<Unit>
    suspend fun getAccountById(id: String): Result<Account?>

}