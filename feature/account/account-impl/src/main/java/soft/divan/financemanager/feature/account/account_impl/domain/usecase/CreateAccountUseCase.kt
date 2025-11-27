package soft.divan.financemanager.feature.account.account_impl.domain.usecase

import soft.divan.financemanager.core.domain.model.AccountBrief

interface CreateAccountUseCase {
    operator fun invoke(account: AccountBrief): Result<Unit>
}