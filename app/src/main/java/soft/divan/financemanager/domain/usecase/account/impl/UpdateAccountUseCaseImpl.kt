package soft.divan.financemanager.domain.usecase.account.impl

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.domain.model.AccountBrief
import soft.divan.financemanager.domain.repository.AccountRepository
import soft.divan.financemanager.domain.usecase.account.UpdateAccountUseCase
import javax.inject.Inject

/**
 * Реализация бизнес-логики для обновления информации об аккаунте.
 *
 * Этот use case инкапсулирует операцию обновления данных аккаунта,
 * делегируя вызов соответствующему методу репозитория [AccountRepository].
 *
 * @property accountRepository репозиторий для работы с аккаунтами
 *
 * @see UpdateAccountUseCase
 * @see AccountRepository
 */

class UpdateAccountUseCaseImpl @Inject constructor(
    private val accountRepository: AccountRepository
) : UpdateAccountUseCase {
    override fun invoke(accountBrief: AccountBrief): Flow<AccountBrief> {
        return accountRepository.updateAccount(accountBrief)
    }

}
