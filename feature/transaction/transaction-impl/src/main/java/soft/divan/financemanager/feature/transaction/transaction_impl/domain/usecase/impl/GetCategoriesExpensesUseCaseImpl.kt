package soft.divan.financemanager.feature.transaction.transaction_impl.domain.usecase.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import soft.divan.financemanager.core.domain.model.Category
import soft.divan.financemanager.core.domain.repository.CategoryRepository
import soft.divan.financemanager.feature.transaction.transaction_impl.domain.usecase.GetCategoriesExpensesUseCase
import javax.inject.Inject

class GetCategoriesExpensesUseCaseImpl @Inject constructor(
    private val categoryRepository: CategoryRepository
) : GetCategoriesExpensesUseCase {
    override suspend fun invoke(): Flow<List<Category>> {
        return categoryRepository.getCategories().map { categories ->
            categories.filter { !it.isIncome }
        }
    }
}