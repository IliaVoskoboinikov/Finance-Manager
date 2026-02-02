package soft.divan.financemanager.core.data.source.impl

import retrofit2.Response
import soft.divan.financemanager.core.data.api.CategoryApiService
import soft.divan.financemanager.core.data.dto.CategoryDto
import soft.divan.financemanager.core.data.source.CategoryRemoteDataSource
import javax.inject.Inject

class CategoryRemoteDataSourceImpl @Inject constructor(
    private val categoryApiService: CategoryApiService,
) : CategoryRemoteDataSource {

    override suspend fun getAll(): Response<List<CategoryDto>> = categoryApiService.getAll()


    override suspend fun getByType(isIncome: Boolean): Response<List<CategoryDto>> =
        categoryApiService.getByType(isIncome)
}
