package soft.divan.financemanager.core.domain.usecase

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.core.domain.model.Account
import soft.divan.financemanager.core.domain.result.DomainResult

interface GetAccountsUseCase {
    suspend operator fun invoke(): Flow<DomainResult<List<Account>>>
}