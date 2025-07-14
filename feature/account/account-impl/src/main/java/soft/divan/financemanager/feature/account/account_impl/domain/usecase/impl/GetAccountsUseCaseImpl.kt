package soft.divan.financemanager.feature.account.account_impl.domain.usecase.impl

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.core.domain.model.Account
import soft.divan.financemanager.core.domain.repository.AccountRepository
import soft.divan.financemanager.feature.account.account_impl.domain.usecase.GetAccountsUseCase
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