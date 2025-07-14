package soft.divan.financemanager.feature.category.category_impl.domain.usecase.impl

import soft.divan.financemanager.core.domain.model.Category
import soft.divan.financemanager.feature.category.category_impl.domain.repository.CategoryRepository
import soft.divan.financemanager.feature.category.category_impl.domain.usecase.GetCategoriesUseCase
import javax.inject.Inject

class GetCategoriesUseCaseImpl @Inject constructor(
    private val categoryRepository: CategoryRepository
) : GetCategoriesUseCase {
    override suspend fun invoke(): Result<List<Category>> {
        return categoryRepository.getCategories()
    }
}
