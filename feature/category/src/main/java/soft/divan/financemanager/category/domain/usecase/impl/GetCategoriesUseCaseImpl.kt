package soft.divan.financemanager.category.domain.usecase.impl

import soft.divan.financemanager.category.domain.model.Category
import soft.divan.financemanager.category.domain.repository.CategoryRepository
import soft.divan.financemanager.category.domain.usecase.GetCategoriesUseCase
import javax.inject.Inject

class GetCategoriesUseCaseImpl @Inject constructor(
    private val categoryRepository: CategoryRepository
) : GetCategoriesUseCase {
    override suspend fun invoke(): Result<List<Category>> {
        return categoryRepository.getCategories()
    }
}
