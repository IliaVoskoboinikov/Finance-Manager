package soft.divan.financemanager.category.domain.usecase

import soft.divan.financemanager.category.domain.model.Category

interface GetCategoriesUseCase {
    suspend operator fun invoke(): Result<List<Category>>
}