package soft.divan.financemanager.feature.account.account_impl.domain.usecase

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.core.domain.model.Account
import soft.divan.financemanager.core.domain.model.CreateAccountRequest


interface CreateAccountUseCase {
    operator fun invoke(createAccountRequest: CreateAccountRequest): Flow<Account>
}
