package soft.divan.financemanager.domain.usecase.category.impl

import soft.divan.financemanager.domain.model.Category
import soft.divan.financemanager.domain.usecase.category.SearchCategoryUseCase
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
