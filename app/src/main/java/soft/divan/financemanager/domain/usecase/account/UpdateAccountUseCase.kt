package soft.divan.financemanager.domain.usecase.account

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.domain.model.AccountBrief

interface UpdateAccountUseCase {
    operator fun invoke(accountBrief: AccountBrief): Flow<AccountBrief>
}
