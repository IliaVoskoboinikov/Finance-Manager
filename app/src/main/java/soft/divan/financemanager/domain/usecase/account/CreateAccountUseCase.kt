package soft.divan.financemanager.domain.usecase.account

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.domain.model.Account
import soft.divan.financemanager.domain.model.CreateAccountRequest

interface CreateAccountUseCase {
    operator fun invoke(createAccountRequest: CreateAccountRequest): Flow<Account>
}
