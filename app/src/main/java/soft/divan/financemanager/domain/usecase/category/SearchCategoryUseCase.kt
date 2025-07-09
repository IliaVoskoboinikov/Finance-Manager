package soft.divan.financemanager.domain.usecase.category

import soft.divan.financemanager.domain.model.Category

interface SearchCategoryUseCase {
    suspend operator fun invoke(query: String, categories: List<Category>): List<Category>
}