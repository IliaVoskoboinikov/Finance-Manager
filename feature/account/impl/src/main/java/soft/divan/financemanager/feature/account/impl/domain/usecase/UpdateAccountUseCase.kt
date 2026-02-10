package soft.divan.financemanager.feature.account.impl.domain.usecase

import soft.divan.financemanager.core.domain.model.Account
import soft.divan.financemanager.core.domain.result.DomainResult

interface UpdateAccountUseCase {
    suspend operator fun invoke(account: Account): DomainResult<Unit>
}
// Revue me>>
