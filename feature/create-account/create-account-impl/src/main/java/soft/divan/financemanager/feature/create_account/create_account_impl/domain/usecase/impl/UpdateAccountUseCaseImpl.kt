package soft.divan.financemanager.feature.create_account.create_account_impl.domain.usecase.impl

import soft.divan.financemanager.core.domain.model.CreateAccountRequest
import soft.divan.financemanager.core.domain.repository.AccountRepository
import soft.divan.financemanager.feature.create_account.create_account_impl.domain.usecase.UpdateAccountUseCase
import javax.inject.Inject

class UpdateAccountUseCaseImpl @Inject constructor(
    private val accountRepository: AccountRepository
) : UpdateAccountUseCase {
    override suspend fun invoke(id: Int, account: CreateAccountRequest): Result<Unit> {
        return accountRepository.updateAccount(id, account)
    }
}