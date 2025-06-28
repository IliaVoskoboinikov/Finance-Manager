package soft.divan.financemanager.domain.usecase.account.impl


import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.domain.model.Account
import soft.divan.financemanager.domain.model.CreateAccountRequest
import soft.divan.financemanager.domain.repository.AccountRepository
import soft.divan.financemanager.domain.usecase.account.CreateAccountUseCase
import javax.inject.Inject

/**
 * Реализация бизнес-логики для создания нового аккаунта.
 *
 * Этот класс представляет собой use case, который инкапсулирует процесс создания аккаунта,
 * делегируя фактическую работу репозиторию [AccountRepository].
 *
 * @property accountRepository репозиторий для работы с аккаунтами
 *
 * @see CreateAccountUseCase
 * @see AccountRepository
 */
class CreateAccountUseCaseImpl @Inject constructor(
    private val accountRepository: AccountRepository
) : CreateAccountUseCase {
    override fun invoke(createAccountRequest: CreateAccountRequest): Flow<Account> {
        return accountRepository.createAccount(createAccountRequest)
    }
}


