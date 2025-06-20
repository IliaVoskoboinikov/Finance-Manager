package soft.divan.financemanager.domain.usecase.account.impl

import soft.divan.financemanager.domain.model.AccountBrief
import soft.divan.financemanager.domain.repository.AccountRepository
import soft.divan.financemanager.domain.usecase.account.UpdateAccountUseCase
import soft.divan.financemanager.domain.utils.Rezult
import javax.inject.Inject

class UpdateAccountUseCaseImpl @Inject constructor(
    private val accountRepository: AccountRepository
): UpdateAccountUseCase {
    override suspend fun invoke(accountBrief: AccountBrief): Rezult<AccountBrief> {
        return accountRepository.updateAccount(accountBrief)
    }

}
