package soft.divan.financemanager.core.data.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import soft.divan.financemanager.core.data.Syncable
import soft.divan.financemanager.core.data.Synchronizer
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
    private val categoryLocalDataSource: CategoryLocalDataSource,
    private val applicationScope: CoroutineScope,
    private val dispatcher: CoroutineDispatcher,
    private val exceptionHandler: CoroutineExceptionHandler
) : CategoryRepository, Syncable {

    //todo возвращает пустой список при сетевой ошибке
    override suspend fun getCategories(): Flow<List<Category>> {
        applicationScope.launch(dispatcher + exceptionHandler) {
            val response = categoryRemoteDataSource.getCategories()
            val categoriesDto = response.body().orEmpty()
            val categoriesDtoEntity = categoriesDto.map { it.toEntity() }
            categoryLocalDataSource.insertCategories(categoriesDtoEntity)
        }
        val categoriesFlow =
            categoryLocalDataSource.getCategories().map { list -> list.map { it.toDomain() } }
        return categoriesFlow
    }

    override suspend fun getCategoriesByType(isIncome: Boolean): Flow<List<Category>> {
        applicationScope.launch(dispatcher + exceptionHandler) {
            val response = categoryRemoteDataSource.getCategoriesByType(isIncome)
            val categoriesDto = response.body().orEmpty()
            val categoriesDtoEntity = categoriesDto.map { it.toEntity() }
            categoryLocalDataSource.insertCategories(categoriesDtoEntity)
        }
        val categoriesFlow =
            categoryLocalDataSource.getCategoriesByType(isIncome)
                .map { list -> list.map { it.toDomain() } }
        return categoriesFlow
    }

    override suspend fun syncWith(synchronizer: Synchronizer): Boolean {
        return runCatching {
            val response = categoryRemoteDataSource.getCategories()
            val categoriesDto = response.body().orEmpty()
            val categoriesDtoEntity = categoriesDto.map { it.toEntity() }
            categoryLocalDataSource.insertCategories(categoriesDtoEntity)
        }.isSuccess
    }


}