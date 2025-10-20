package soft.divan.financemanager.feature.transaction.transaction_impl.domain.usecase

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.core.domain.model.Category

interface GetCategoriesExpensesUseCase {
    suspend operator fun invoke(): Flow<List<Category>>
}