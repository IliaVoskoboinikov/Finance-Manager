package soft.divan.finansemanager.account.domain.usecase

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.core.domain.model.Account
import soft.divan.financemanager.core.domain.model.CreateAccountRequest


interface CreateAccountUseCase {
    operator fun invoke(createAccountRequest: CreateAccountRequest): Flow<Account>
}
