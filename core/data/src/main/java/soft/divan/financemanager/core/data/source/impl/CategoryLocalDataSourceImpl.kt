package soft.divan.financemanager.core.data.source.impl

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.core.data.source.CategoryLocalDataSource
import soft.divan.finansemanager.core.database.dao.CategoryDao
import soft.divan.finansemanager.core.database.entity.CategoryEntity
import javax.inject.Inject

class CategoryLocalDataSourceImpl @Inject constructor(
    private val categoryDao: CategoryDao,
) : CategoryLocalDataSource {
    override suspend fun getCategories(): Flow<List<CategoryEntity>> {
        return categoryDao.getCategories()
    }

    override suspend fun insertCategories(categories: List<CategoryEntity>) {
        return categoryDao.insertCategories(categories)
    }

    override suspend fun getCategoriesByType(isIncome: Boolean): Flow<List<CategoryEntity>> {
        return categoryDao.getByType(isIncome)
    }

}