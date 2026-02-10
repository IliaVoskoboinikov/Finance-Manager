package soft.divan.financemanager.core.domain.repository

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.core.domain.model.Category
import soft.divan.financemanager.core.domain.result.DomainResult

interface CategoryRepository {
    fun getAll(): Flow<DomainResult<List<Category>>>
    fun getByType(isIncome: Boolean): Flow<DomainResult<List<Category>>>
}
// Revue me>>
