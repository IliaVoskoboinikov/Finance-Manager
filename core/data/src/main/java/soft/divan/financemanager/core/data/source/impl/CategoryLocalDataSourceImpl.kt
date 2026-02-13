package soft.divan.financemanager.core.data.source.impl

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.core.data.source.CategoryLocalDataSource
import soft.divan.financemanager.core.database.dao.CategoryDao
import soft.divan.financemanager.core.database.entity.CategoryEntity
import javax.inject.Inject

class CategoryLocalDataSourceImpl @Inject constructor(
    private val categoryDao: CategoryDao
) : CategoryLocalDataSource {

    override suspend fun insert(categories: List<CategoryEntity>) =
        categoryDao.insertAll(categories)

    override suspend fun getAll(): Flow<List<CategoryEntity>> = categoryDao.getAll()

    override suspend fun getByType(isIncome: Boolean): Flow<List<CategoryEntity>> =
        categoryDao.getByType(isIncome)
}
