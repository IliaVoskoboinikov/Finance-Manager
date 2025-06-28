package soft.divan.financemanager.domain.usecase.account

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.domain.model.Account

interface GetAccountsUseCase {
     operator fun invoke(): Flow<List<Account>>
}
