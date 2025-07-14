package soft.divan.financemanager.feature.account.account_impl.domain.usecase

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.core.domain.model.AccountBrief

interface UpdateAccountUseCase {
    operator fun invoke(accountBrief: AccountBrief): Flow<AccountBrief>
}
