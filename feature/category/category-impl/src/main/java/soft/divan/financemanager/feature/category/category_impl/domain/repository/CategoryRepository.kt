package soft.divan.financemanager.feature.category.category_impl.domain.repository

import soft.divan.financemanager.core.domain.model.Category

interface CategoryRepository {
    suspend fun getCategories(): Result<List<Category>>
}