package soft.divan.financemanager.domain.usecase.account.impl


import soft.divan.financemanager.domain.model.Account
import soft.divan.financemanager.domain.model.CreateAccountRequest
import soft.divan.financemanager.domain.repository.AccountRepository
import soft.divan.financemanager.domain.usecase.account.CreateAccountUseCase
import soft.divan.financemanager.domain.utils.Rezult
import javax.inject.Inject

class CreateAccountUseCaseImpl @Inject constructor(
    private val accountRepository: AccountRepository
) : CreateAccountUseCase {
    override suspend fun invoke(createAccountRequest: CreateAccountRequest): Rezult<Account> {
        return accountRepository.createAccount(createAccountRequest)
    }
}