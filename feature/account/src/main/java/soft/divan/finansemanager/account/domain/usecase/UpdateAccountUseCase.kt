package soft.divan.finansemanager.account.domain.usecase

import kotlinx.coroutines.flow.Flow
import soft.divan.finansemanager.account.domain.model.AccountBrief

interface UpdateAccountUseCase {
    operator fun invoke(accountBrief: AccountBrief): Flow<AccountBrief>
}
