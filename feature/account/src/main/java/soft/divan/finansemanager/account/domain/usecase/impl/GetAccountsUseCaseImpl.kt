package soft.divan.finansemanager.account.domain.usecase.impl

import kotlinx.coroutines.flow.Flow
import soft.divan.finansemanager.account.domain.model.Account
import soft.divan.finansemanager.account.domain.repository.AccountRepository
import soft.divan.finansemanager.account.domain.usecase.GetAccountsUseCase
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