package soft.divan.financemanager.domain.repository

import soft.divan.financemanager.domain.model.Category

interface CategoryRepository {
    suspend fun getCategories(): Result<List<Category>>
}