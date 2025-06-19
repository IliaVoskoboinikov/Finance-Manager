package soft.divan.financemanager.domain.usecase.account.impl

import jakarta.inject.Inject
import soft.divan.financemanager.domain.model.Account
import soft.divan.financemanager.domain.repository.AccountRepository
import soft.divan.financemanager.domain.usecase.account.GetAccountsUseCase
import soft.divan.financemanager.domain.utils.Rezult

class GetAccountsUseCaseImpl @Inject constructor(
    private val accountRepository: AccountRepository
) : GetAccountsUseCase {
    override suspend fun invoke(): Rezult<List<Account>> {

        return accountRepository.getAccounts()
    }
}