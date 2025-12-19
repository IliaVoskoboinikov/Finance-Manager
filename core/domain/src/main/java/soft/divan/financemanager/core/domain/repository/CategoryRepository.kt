package soft.divan.financemanager.core.domain.repository

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.core.domain.model.Category
import soft.divan.financemanager.core.domain.result.DomainResult

interface CategoryRepository {
    suspend fun getCategories(): Flow<DomainResult<List<Category>>>
    suspend fun getCategoriesByType(isIncome: Boolean): Flow<List<Category>>
}