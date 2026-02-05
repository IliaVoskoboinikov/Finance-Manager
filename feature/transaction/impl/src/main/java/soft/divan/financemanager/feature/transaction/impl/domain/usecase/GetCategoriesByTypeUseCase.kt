package soft.divan.financemanager.feature.transaction.impl.domain.usecase

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.core.domain.model.Category
import soft.divan.financemanager.core.domain.result.DomainResult

interface GetCategoriesByTypeUseCase {
    operator fun invoke(isIncome: Boolean): Flow<DomainResult<List<Category>>>
}
