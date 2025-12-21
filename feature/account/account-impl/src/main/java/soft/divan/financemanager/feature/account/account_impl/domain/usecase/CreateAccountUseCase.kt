package soft.divan.financemanager.feature.account.account_impl.domain.usecase

import soft.divan.financemanager.core.domain.model.Account
import soft.divan.financemanager.core.domain.result.DomainResult

interface CreateAccountUseCase {
    suspend operator fun invoke(account: Account): DomainResult<Unit>
}