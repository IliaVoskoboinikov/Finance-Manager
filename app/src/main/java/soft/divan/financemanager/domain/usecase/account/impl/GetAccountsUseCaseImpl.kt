package soft.divan.financemanager.domain.usecase.account.impl

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.domain.model.Account
import soft.divan.financemanager.domain.repository.AccountRepository
import soft.divan.financemanager.domain.usecase.account.GetAccountsUseCase
import soft.divan.financemanager.domain.utils.Rezult
import javax.inject.Inject

class GetAccountsUseCaseImpl @Inject constructor(
    private val accountRepository: AccountRepository
) : GetAccountsUseCase {
    override fun invoke(): Flow<List<Account>> {
        return accountRepository.getAccounts()
    }
}