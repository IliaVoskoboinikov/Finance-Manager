package soft.divan.financemanager.core.data.repository

import android.util.Log
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import soft.divan.financemanager.core.data.mapper.toDomain
import soft.divan.financemanager.core.data.mapper.toEntity
import soft.divan.financemanager.core.data.source.CategoryLocalDataSource
import soft.divan.financemanager.core.data.source.CategoryRemoteDataSource
import soft.divan.financemanager.core.domain.model.Category
import soft.divan.financemanager.core.domain.repository.CategoryRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val categoryRemoteDataSource: CategoryRemoteDataSource,
    private val categoryLocalDataSource: CategoryLocalDataSource

) : CategoryRepository {

    private val scope = CoroutineScope(
        SupervisorJob()
                + Dispatchers.IO
                + CoroutineExceptionHandler { _, t -> Log.w("CategoryRepository", t) }
    )

    override suspend fun getCategories(): Flow<List<Category>> {
        scope.launch {
            val response = categoryRemoteDataSource.getCategories()
            val categoriesDto = response.body().orEmpty()
            val categoriesDtoEntity = categoriesDto.map { it.toEntity() }
            categoryLocalDataSource.insertCategories(categoriesDtoEntity)
        }
        val categoriesFlow =
            categoryLocalDataSource.getCategories().map { list -> list.map { it.toDomain() } }
        return categoriesFlow
    }


}