package soft.divan.financemanager.data.source

import retrofit2.Response
import soft.divan.financemanager.data.network.dto.CategoryDto

interface CategoryRemoteDataSource {
    suspend fun getCategories(): Response<List<CategoryDto>>
}