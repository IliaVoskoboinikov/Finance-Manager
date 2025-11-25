package soft.divan.financemanager.core.domain.usecase.impl

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.core.domain.model.Account
import soft.divan.financemanager.core.domain.repository.AccountRepository
import soft.divan.financemanager.core.domain.usecase.GetAccountsUseCase
import javax.inject.Inject

class GetAccountsUseCaseImpl @Inject constructor(
    private val accountRepository: AccountRepository
) : GetAccountsUseCase {
    override suspend fun invoke(): Flow<List<Account>> {
        return accountRepository.getAccounts()
    }
}