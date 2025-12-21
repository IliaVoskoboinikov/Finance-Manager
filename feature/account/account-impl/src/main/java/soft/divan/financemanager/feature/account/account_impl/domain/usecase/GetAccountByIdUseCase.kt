package soft.divan.financemanager.feature.account.account_impl.domain.usecase

import soft.divan.financemanager.core.domain.model.Account
import soft.divan.financemanager.core.domain.result.DomainResult

interface GetAccountByIdUseCase {
    suspend operator fun invoke(id: String): DomainResult<Account>
}