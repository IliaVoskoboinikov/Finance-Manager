package soft.divan.financemanager.feature.account.impl.domain.usecase.impl

import soft.divan.financemanager.core.domain.model.Account
import soft.divan.financemanager.core.domain.repository.AccountRepository
import soft.divan.financemanager.core.domain.result.DomainResult
import soft.divan.financemanager.feature.account.impl.domain.usecase.CreateAccountUseCase
import javax.inject.Inject

class CreateAccountUseCaseImpl @Inject constructor(
    private val accountRepository: AccountRepository
) : CreateAccountUseCase {
    override suspend fun invoke(account: Account): DomainResult<Unit> {
        return accountRepository.create(account)
    }
}
// Revue me>>
