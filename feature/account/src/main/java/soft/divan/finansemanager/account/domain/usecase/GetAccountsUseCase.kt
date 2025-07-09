package soft.divan.finansemanager.account.domain.usecase

import kotlinx.coroutines.flow.Flow
import soft.divan.finansemanager.account.domain.model.Account

interface GetAccountsUseCase {
    operator fun invoke(): Flow<List<Account>>
}
