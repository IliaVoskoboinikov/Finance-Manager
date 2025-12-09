package soft.divan.financemanager.feature.account.account_impl.domain.usecase

import soft.divan.financemanager.core.domain.model.Account

interface CreateAccountUseCase {
    suspend operator fun invoke(account: Account): Result<Unit>
}