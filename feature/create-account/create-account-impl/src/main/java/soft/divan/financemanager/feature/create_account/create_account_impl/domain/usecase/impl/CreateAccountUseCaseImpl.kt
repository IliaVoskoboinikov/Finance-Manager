package soft.divan.financemanager.feature.create_account.create_account_impl.domain.usecase.impl

import soft.divan.financemanager.core.domain.model.CreateAccountRequest
import soft.divan.financemanager.core.domain.repository.AccountRepository
import soft.divan.financemanager.feature.create_account.create_account_impl.domain.usecase.CreateAccountUseCase
import javax.inject.Inject

class CreateAccountUseCaseImpl @Inject constructor(
    private val accountRepository: AccountRepository
) : CreateAccountUseCase {
    override fun invoke(createAccountRequest: CreateAccountRequest): Result<Unit> {
        return accountRepository.createAccount(createAccountRequest)
    }
}