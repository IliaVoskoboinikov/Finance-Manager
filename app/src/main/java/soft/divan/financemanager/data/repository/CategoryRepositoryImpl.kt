package soft.divan.financemanager.data.repository

import soft.divan.financemanager.data.mapper.toDomain
import soft.divan.financemanager.data.mapper.toEntity
import soft.divan.financemanager.data.source.CategoryRemoteDataSource
import soft.divan.financemanager.domain.model.Category
import soft.divan.financemanager.domain.repository.CategoryRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val categoryRemoteDataSource: CategoryRemoteDataSource
) : CategoryRepository {

    override suspend fun getCategories(): Result<List<Category>> = runCatching {
        val response = categoryRemoteDataSource.getCategories()
        val categoryDto = response.body().orEmpty()
        val categoryDtoEntity = categoryDto.map { it.toEntity() }
        val category = categoryDtoEntity.map { it.toDomain() }
        return Result.success(category)
    }


}
