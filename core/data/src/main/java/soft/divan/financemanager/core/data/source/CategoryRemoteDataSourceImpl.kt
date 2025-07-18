package soft.divan.financemanager.core.data.source

import retrofit2.Response
import soft.divan.financemanager.core.data.api.CategoryApiService
import soft.divan.financemanager.core.data.dto.CategoryDto
import javax.inject.Inject

class CategoryRemoteDataSourceImpl @Inject constructor(
    private val categoryApiService: CategoryApiService,
) : CategoryRemoteDataSource {
    override suspend fun getCategories(): Response<List<CategoryDto>> {
        return categoryApiService.getCategories()
    }
}