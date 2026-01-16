package soft.divan.financemanager.core.domain.repository

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.core.domain.model.Category
import soft.divan.financemanager.core.domain.result.DomainResult

interface CategoryRepository {
    fun getCategories(): Flow<DomainResult<List<Category>>>
    fun getCategoriesByType(isIncome: Boolean): Flow<DomainResult<List<Category>>>
}