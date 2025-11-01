package soft.divan.financemanager.feature.account.account_impl.domain.usecase.impl

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.core.domain.model.Account
import soft.divan.financemanager.core.domain.model.CreateAccountRequest
import soft.divan.financemanager.core.domain.repository.AccountRepository
import soft.divan.financemanager.feature.account.account_impl.domain.usecase.CreateAccountUseCase
import javax.inject.Inject

class CreateAccountUseCaseImpl @Inject constructor(
    private val accountRepository: AccountRepository
) : CreateAccountUseCase {
    override fun invoke(createAccountRequest: CreateAccountRequest): Flow<Account> {
        return accountRepository.createAccount(createAccountRequest)
    }
}