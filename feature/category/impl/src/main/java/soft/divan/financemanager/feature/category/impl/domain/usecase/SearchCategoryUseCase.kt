package soft.divan.financemanager.feature.category.impl.domain.usecase

import soft.divan.financemanager.core.domain.model.Category

interface SearchCategoryUseCase {
    suspend operator fun invoke(query: String, categories: List<Category>): List<Category>
}
// Revue me>>
