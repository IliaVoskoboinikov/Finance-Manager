package soft.divan.financemanager.core.data.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import soft.divan.financemanager.core.data.dto.CategoryDto

interface CategoryApiService {
    @GET("v1/categories")
    suspend fun getCategories(): Response<List<CategoryDto>>

    @GET("v1/categories/type/{isIncome}")
    suspend fun getCategoriesByType(@Path("isIncome") isIncome: Boolean): Response<List<CategoryDto>>
}