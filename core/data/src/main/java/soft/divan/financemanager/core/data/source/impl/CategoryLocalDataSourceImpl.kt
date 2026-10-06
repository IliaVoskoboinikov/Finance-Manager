package soft.divan.financemanager.core.data.source.impl

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.core.data.source.CategoryLocalDataSource
import soft.divan.financemanager.core.database.entity.CategoryEntity
import soft.divan.financemanager.core.database.holder.DatabaseHolder
import javax.inject.Inject

class CategoryLocalDataSourceImpl @Inject constructor(
    private val holder: DatabaseHolder
) : CategoryLocalDataSource {

    override suspend fun insert(categories: List<CategoryEntity>) =
        holder.withDatabase { it.categoryDao().insertAll(categories) }

    override suspend fun getAll(): Flow<List<CategoryEntity>> =
        holder.observe { it.categoryDao().getAll() }

    override suspend fun getByType(isIncome: Boolean): Flow<List<CategoryEntity>> =
        holder.observe { it.categoryDao().getByType(isIncome) }

    override suspend fun getById(id: String): CategoryEntity? =
        holder.withDatabase { it.categoryDao().getById(id) }
}
