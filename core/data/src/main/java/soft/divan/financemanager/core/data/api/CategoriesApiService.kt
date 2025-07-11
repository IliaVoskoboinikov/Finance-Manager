package soft.divan.financemanager.core.data.api

import retrofit2.Response
import retrofit2.http.GET
import soft.divan.financemanager.core.data.dto.CategoryDto

interface CategoryApiService {
    @GET("v1/categories")
    suspend fun getCategories(): Response<List<CategoryDto>>
}