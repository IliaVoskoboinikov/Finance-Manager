package soft.divan.financemanager.category.domain.usecase

import soft.divan.financemanager.core.domain.model.Category

interface SearchCategoryUseCase {
    suspend operator fun invoke(query: String, categories: List<Category>): List<Category>
}