package soft.divan.financemanager.domain.usecase.account.impl


import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.domain.model.Account
import soft.divan.financemanager.domain.model.CreateAccountRequest
import soft.divan.financemanager.domain.repository.AccountRepository
import soft.divan.financemanager.domain.usecase.account.CreateAccountUseCase
import javax.inject.Inject

class CreateAccountUseCaseImpl @Inject constructor(
    private val accountRepository: AccountRepository
) : CreateAccountUseCase {
    override fun invoke(createAccountRequest: CreateAccountRequest): Flow<Account> {
        return accountRepository.createAccount(createAccountRequest)
    }
}