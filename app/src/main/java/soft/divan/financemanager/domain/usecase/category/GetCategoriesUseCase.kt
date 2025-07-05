package soft.divan.financemanager.domain.usecase.category

import soft.divan.financemanager.domain.model.Category

interface GetCategoriesUseCase {
    suspend operator fun invoke(): Result<List<Category>>
}