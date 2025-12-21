package soft.divan.financemanager.feature.account.account_impl.domain.usecase.impl

import soft.divan.financemanager.core.domain.repository.AccountRepository
import soft.divan.financemanager.core.domain.result.DomainResult
import soft.divan.financemanager.feature.account.account_impl.domain.usecase.DeleteAccountUseCase
import javax.inject.Inject

class DeleteAccountUseCaseImpl @Inject constructor(
    private val accountRepository: AccountRepository
) : DeleteAccountUseCase {
    override suspend fun invoke(id: String): DomainResult<Unit> {
        return accountRepository.deleteAccount(id)
    }
}