package soft.divan.financemanager.category.domain.repository

import soft.divan.financemanager.category.domain.model.Category

interface CategoryRepository {
    suspend fun getCategories(): Result<List<Category>>
}