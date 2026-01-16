package soft.divan.financemanager.core.domain.repository

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.core.domain.model.Account
import soft.divan.financemanager.core.domain.result.DomainResult

interface AccountRepository {
    fun getAccounts(): Flow<DomainResult<List<Account>>>
    suspend fun createAccount(account: Account): DomainResult<Unit>
    suspend fun updateAccount(account: Account): DomainResult<Unit>
    suspend fun deleteAccount(id: String): DomainResult<Unit>
    suspend fun getAccountById(id: String): DomainResult<Account>
}