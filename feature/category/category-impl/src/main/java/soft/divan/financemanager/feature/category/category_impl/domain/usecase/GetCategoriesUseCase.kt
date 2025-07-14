package soft.divan.financemanager.feature.category.category_impl.domain.usecase

import soft.divan.financemanager.core.domain.model.Category

interface GetCategoriesUseCase {
    suspend operator fun invoke(): Result<List<Category>>
}