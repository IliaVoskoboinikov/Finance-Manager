package soft.divan.financemanager.feature.create_account.create_account_impl.domain.usecase.impl

import soft.divan.financemanager.core.domain.model.AccountBrief
import soft.divan.financemanager.core.domain.repository.AccountRepository
import soft.divan.financemanager.feature.create_account.create_account_impl.domain.usecase.CreateAccountUseCase
import javax.inject.Inject

class CreateAccountUseCaseImpl @Inject constructor(
    private val accountRepository: AccountRepository
) : CreateAccountUseCase {
    override fun invoke(account: AccountBrief): Result<Unit> {
        return accountRepository.createAccount(account)
    }
}