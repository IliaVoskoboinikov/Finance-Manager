package soft.divan.financemanager.core.domain.usecase

import soft.divan.financemanager.core.domain.model.Category
import soft.divan.financemanager.core.domain.repository.CategoryRepository
import javax.inject.Inject

class GetCategoriesUseCaseImpl @Inject constructor(
    private val categoryRepository: CategoryRepository
) : GetCategoriesUseCase {
    override suspend fun invoke(): Result<List<Category>> {
        return categoryRepository.getCategories()
    }
}