package soft.divan.financemanager.domain.usecase.account

import soft.divan.financemanager.domain.model.AccountBrief
import soft.divan.financemanager.domain.utils.Rezult

interface UpdateAccountUseCase {
    suspend operator fun invoke(accountBrief: AccountBrief): Rezult<AccountBrief>
}