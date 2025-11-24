package soft.divan.financemanager.feature.create_account.create_account_impl.domain.usecase

import soft.divan.financemanager.core.domain.model.CreateAccountRequest

interface CreateAccountUseCase {
    operator fun invoke(createAccountRequest: CreateAccountRequest): Result<Unit>
}