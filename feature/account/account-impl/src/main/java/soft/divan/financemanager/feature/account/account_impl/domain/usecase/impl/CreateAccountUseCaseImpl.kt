package soft.divan.financemanager.feature.account.account_impl.domain.usecase.impl

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.core.domain.model.Account
import soft.divan.financemanager.core.domain.model.CreateAccountRequest
import soft.divan.financemanager.core.domain.repository.AccountRepository
import soft.divan.financemanager.feature.account.account_impl.domain.usecase.CreateAccountUseCase
import javax.inject.Inject

/**
 * Реализация бизнес-логики для создания нового аккаунта.
 *
 * Этот класс представляет собой use case, который инкапсулирует процесс создания аккаунта,
 * делегируя фактическую работу репозиторию [soft.divan.financemanager.domain.repository.AccountRepository].
 *
 * @property accountRepository репозиторий для работы с аккаунтами
 *
 * @see soft.divan.finansemanager.account.domain.usecase.CreateAccountUseCase
 * @see soft.divan.financemanager.domain.repository.AccountRepository
 */
class CreateAccountUseCaseImpl @Inject constructor(
    private val accountRepository: AccountRepository
) : CreateAccountUseCase {
    override fun invoke(createAccountRequest: CreateAccountRequest): Flow<Account> {
        return accountRepository.createAccount(createAccountRequest)
    }
}