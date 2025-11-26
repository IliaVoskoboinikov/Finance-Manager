package soft.divan.financemanager.feature.create_account.create_account_impl.domain.usecase

import soft.divan.financemanager.core.domain.model.Account

interface GetAccountByIdUseCase {
    suspend operator fun invoke(id: Int): Result<Account>
}