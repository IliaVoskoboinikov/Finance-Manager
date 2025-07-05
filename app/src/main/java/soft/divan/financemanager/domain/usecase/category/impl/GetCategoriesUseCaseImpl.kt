package soft.divan.financemanager.domain.usecase.category.impl

import soft.divan.financemanager.domain.model.Category
import soft.divan.financemanager.domain.repository.CategoryRepository
import soft.divan.financemanager.domain.usecase.category.GetCategoriesUseCase
import javax.inject.Inject

class GetCategoriesUseCaseImpl @Inject constructor(
    private val categoryRepository: CategoryRepository
) : GetCategoriesUseCase {
    override suspend fun invoke(): Result<List<Category>> {
        return categoryRepository.getCategories()
    }
}
