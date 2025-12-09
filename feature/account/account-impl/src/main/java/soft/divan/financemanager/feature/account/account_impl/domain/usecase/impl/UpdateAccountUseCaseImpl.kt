package soft.divan.financemanager.feature.account.account_impl.domain.usecase.impl

import soft.divan.financemanager.core.domain.model.Account
import soft.divan.financemanager.core.domain.repository.AccountRepository
import soft.divan.financemanager.feature.account.account_impl.domain.usecase.UpdateAccountUseCase
import javax.inject.Inject

class UpdateAccountUseCaseImpl @Inject constructor(
    private val accountRepository: AccountRepository
) : UpdateAccountUseCase {
    override suspend fun invoke(account: Account): Result<Unit> {
        return accountRepository.updateAccount(account)
    }
}