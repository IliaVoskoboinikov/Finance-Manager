package soft.divan.financemanager.feature.transaction.impl.domain.usecase.impl

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.core.domain.model.Category
import soft.divan.financemanager.core.domain.repository.CategoryRepository
import soft.divan.financemanager.core.domain.result.DomainResult
import soft.divan.financemanager.feature.transaction.impl.domain.usecase.GetCategoriesByTypeUseCase
import javax.inject.Inject

class GetCategoriesByTypeUseCaseImpl @Inject constructor(
    private val categoryRepository: CategoryRepository
) : GetCategoriesByTypeUseCase {
    override fun invoke(isIncome: Boolean): Flow<DomainResult<List<Category>>> {
        return categoryRepository.getByType(isIncome)
    }
}