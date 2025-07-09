package soft.divan.financemanager.category.data.source

import retrofit2.Response
import soft.divan.financemanager.core.network.api.CategoryApiService
import soft.divan.financemanager.core.network.dto.CategoryDto
import javax.inject.Inject

class CategoryRemoteDataSourceImpl @Inject constructor(
    private val categoryApiService: CategoryApiService,
) : CategoryRemoteDataSource {
    override suspend fun getCategories(): Response<List<CategoryDto>> {
        return categoryApiService.getCategories()
    }
}