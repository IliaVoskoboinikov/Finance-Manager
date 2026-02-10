package soft.divan.financemanager.core.domain.repository

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.core.domain.model.Account
import soft.divan.financemanager.core.domain.result.DomainResult

interface AccountRepository {
    suspend fun create(account: Account): DomainResult<Unit>
    fun getAll(): Flow<DomainResult<List<Account>>>
    suspend fun update(account: Account): DomainResult<Unit>
    suspend fun delete(id: String): DomainResult<Unit>
    suspend fun getById(id: String): DomainResult<Account>
}
// Revue me>>
