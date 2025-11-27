package soft.divan.financemanager.feature.create_account.create_account_impl.domain.usecase.impl

import soft.divan.financemanager.core.domain.model.AccountBrief
import soft.divan.financemanager.core.domain.repository.AccountRepository
import soft.divan.financemanager.feature.create_account.create_account_impl.domain.usecase.UpdateAccountUseCase
import javax.inject.Inject

class UpdateAccountUseCaseImpl @Inject constructor(
    private val accountRepository: AccountRepository
) : UpdateAccountUseCase {
    override suspend fun invoke(account: AccountBrief): Result<Unit> {
        return accountRepository.updateAccount(account)
    }
}