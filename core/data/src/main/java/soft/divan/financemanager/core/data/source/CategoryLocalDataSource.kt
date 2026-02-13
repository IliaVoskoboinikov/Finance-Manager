package soft.divan.financemanager.core.data.source

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.core.database.entity.CategoryEntity

interface CategoryLocalDataSource {
    suspend fun insert(categories: List<CategoryEntity>)
    suspend fun getAll(): Flow<List<CategoryEntity>>
    suspend fun getByType(isIncome: Boolean): Flow<List<CategoryEntity>>
}
