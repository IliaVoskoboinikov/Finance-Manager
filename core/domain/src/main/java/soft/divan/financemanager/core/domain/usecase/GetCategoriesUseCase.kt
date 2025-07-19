package soft.divan.financemanager.core.domain.usecase

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.core.domain.model.Category

interface GetCategoriesUseCase {
    suspend operator fun invoke(): Flow<List<Category>>
}