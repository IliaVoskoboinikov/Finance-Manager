package soft.divan.financemanager.category.domain.usecase.impl

import soft.divan.financemanager.category.domain.model.Category
import soft.divan.financemanager.category.domain.usecase.SearchCategoryUseCase
import javax.inject.Inject

class SearchCategoryUseCaseImpl @Inject constructor(
) : SearchCategoryUseCase {
    override suspend fun invoke(
        query: String,
        categories: List<Category>
    ): List<Category> {
        return categories.filter {
            it.name.contains(query, ignoreCase = true)
        }
    }
}
