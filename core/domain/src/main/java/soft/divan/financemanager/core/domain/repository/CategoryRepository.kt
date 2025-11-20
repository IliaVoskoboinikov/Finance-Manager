package soft.divan.financemanager.core.domain.repository

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.core.domain.model.Category

interface CategoryRepository {
    suspend fun getCategories(): Flow<List<Category>>
    suspend fun getCategoriesByType(isIncome: Boolean): Flow<List<Category>>
}