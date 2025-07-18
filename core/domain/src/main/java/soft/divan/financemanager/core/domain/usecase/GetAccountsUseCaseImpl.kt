package soft.divan.financemanager.core.domain.usecase

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.core.domain.model.Account
import soft.divan.financemanager.core.domain.repository.AccountRepository
import javax.inject.Inject

/**
 * Реализация бизнес-логики для получения списка всех аккаунтов пользователя.
 *
 * Данный use case инкапсулирует процесс получения аккаунтов,
 * делегируя операцию репозиторию [soft.divan.financemanager.domain.repository.AccountRepository].
 *
 * @property accountRepository репозиторий для работы с аккаунтами
 *
 * @see soft.divan.finansemanager.account.domain.usecase.GetAccountsUseCase
 * @see soft.divan.financemanager.domain.repository.AccountRepository
 */
class GetAccountsUseCaseImpl @Inject constructor(
    private val accountRepository: AccountRepository
) : GetAccountsUseCase {
    override fun invoke(): Flow<List<Account>> {
        return accountRepository.getAccounts()
    }
}