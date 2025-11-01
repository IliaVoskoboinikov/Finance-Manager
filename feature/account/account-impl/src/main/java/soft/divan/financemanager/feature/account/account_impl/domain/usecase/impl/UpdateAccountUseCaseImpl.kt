package soft.divan.financemanager.feature.account.account_impl.domain.usecase.impl

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.core.domain.model.AccountBrief
import soft.divan.financemanager.core.domain.repository.AccountRepository
import soft.divan.financemanager.feature.account.account_impl.domain.usecase.UpdateAccountUseCase
import javax.inject.Inject

class UpdateAccountUseCaseImpl @Inject constructor(
    private val accountRepository: AccountRepository
) : UpdateAccountUseCase {
    override fun invoke(accountBrief: AccountBrief): Flow<AccountBrief> {
        return accountRepository.updateAccount(accountBrief)
    }
}