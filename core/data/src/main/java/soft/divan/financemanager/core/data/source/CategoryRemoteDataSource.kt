package soft.divan.financemanager.core.data.source

import retrofit2.Response
import soft.divan.financemanager.core.data.dto.CategoryDto

interface CategoryRemoteDataSource {
    suspend fun getCategories(): Response<List<CategoryDto>>
}