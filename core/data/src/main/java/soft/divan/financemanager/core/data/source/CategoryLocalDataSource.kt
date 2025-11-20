package soft.divan.financemanager.core.data.source

import kotlinx.coroutines.flow.Flow
import soft.divan.finansemanager.core.database.entity.CategoryEntity

interface CategoryLocalDataSource {
    suspend fun getCategories(): Flow<List<CategoryEntity>>
    suspend fun insertCategories(categories: List<CategoryEntity>)
    suspend fun getCategoriesByType(isIncome: Boolean): Flow<List<CategoryEntity>>
}