package soft.divan.financemanager.domain.usecase.account.impl

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.domain.model.Account
import soft.divan.financemanager.domain.repository.AccountRepository
import soft.divan.financemanager.domain.usecase.account.GetAccountsUseCase
import javax.inject.Inject

/**
 * Реализация бизнес-логики для получения списка всех аккаунтов пользователя.
 *
 * Данный use case инкапсулирует процесс получения аккаунтов,
 * делегируя операцию репозиторию [AccountRepository].
 *
 * @property accountRepository репозиторий для работы с аккаунтами
 *
 * @see GetAccountsUseCase
 * @see AccountRepository
 */
class GetAccountsUseCaseImpl @Inject constructor(
    private val accountRepository: AccountRepository
) : GetAccountsUseCase {
    override fun invoke(): Flow<List<Account>> {
        return accountRepository.getAccounts()
    }
}
