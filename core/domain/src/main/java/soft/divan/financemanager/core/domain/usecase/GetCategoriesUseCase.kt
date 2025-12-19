package soft.divan.financemanager.core.domain.usecase

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.core.domain.model.Category
import soft.divan.financemanager.core.domain.result.DomainResult

interface GetCategoriesUseCase {
    suspend operator fun invoke(): Flow<DomainResult<List<Category>>>
}