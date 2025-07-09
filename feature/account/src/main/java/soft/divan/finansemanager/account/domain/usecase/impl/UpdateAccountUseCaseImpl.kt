package soft.divan.finansemanager.account.domain.usecase.impl

import kotlinx.coroutines.flow.Flow
import soft.divan.finansemanager.account.domain.model.AccountBrief
import soft.divan.finansemanager.account.domain.repository.AccountRepository
import soft.divan.finansemanager.account.domain.usecase.UpdateAccountUseCase
import javax.inject.Inject

/**
 * Реализация бизнес-логики для обновления информации об аккаунте.
 *
 * Этот use case инкапсулирует операцию обновления данных аккаунта,
 * делегируя вызов соответствующему методу репозитория [soft.divan.financemanager.domain.repository.AccountRepository].
 *
 * @property accountRepository репозиторий для работы с аккаунтами
 *
 * @see soft.divan.finansemanager.account.domain.usecase.UpdateAccountUseCase
 * @see soft.divan.financemanager.domain.repository.AccountRepository
 */

class UpdateAccountUseCaseImpl @Inject constructor(
    private val accountRepository: AccountRepository
) : UpdateAccountUseCase {
    override fun invoke(accountBrief: AccountBrief): Flow<AccountBrief> {
        return accountRepository.updateAccount(accountBrief)
    }

}