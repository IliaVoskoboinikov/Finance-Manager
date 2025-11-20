package soft.divan.financemanager.feature.transaction.transaction_impl.domain.usecase

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.core.domain.model.Category

interface GetCategoriesByTypeUseCase {
    suspend operator fun invoke(isIncome: Boolean): Flow<List<Category>>
}