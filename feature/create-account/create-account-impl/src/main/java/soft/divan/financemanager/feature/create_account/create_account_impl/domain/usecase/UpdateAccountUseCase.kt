package soft.divan.financemanager.feature.create_account.create_account_impl.domain.usecase

import soft.divan.financemanager.core.domain.model.AccountBrief

interface UpdateAccountUseCase {
    suspend operator fun invoke(account: AccountBrief): Result<Unit>
}