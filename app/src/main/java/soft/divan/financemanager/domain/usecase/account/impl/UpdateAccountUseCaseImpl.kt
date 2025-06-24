package soft.divan.financemanager.domain.usecase.account.impl

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.domain.model.AccountBrief
import soft.divan.financemanager.domain.repository.AccountRepository
import soft.divan.financemanager.domain.usecase.account.UpdateAccountUseCase
import javax.inject.Inject

class UpdateAccountUseCaseImpl @Inject constructor(
    private val accountRepository: AccountRepository
) : UpdateAccountUseCase {
    override fun invoke(accountBrief: AccountBrief): Flow<AccountBrief> {
        return accountRepository.updateAccount(accountBrief)
    }

}
