package soft.divan.financemanager.core.data.source

import retrofit2.Response
import soft.divan.financemanager.core.data.dto.CategoryDto

interface CategoryRemoteDataSource {
    suspend fun getAll(): Response<List<CategoryDto>>
    suspend fun getByType(isIncome: Boolean): Response<List<CategoryDto>>
}
// Revue me>>
