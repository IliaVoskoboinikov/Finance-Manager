package soft.divan.financemanager.feature.account.account_impl.domain.usecase.impl

import soft.divan.financemanager.core.domain.model.Account
import soft.divan.financemanager.core.domain.repository.AccountRepository
import soft.divan.financemanager.core.domain.result.DomainResult
import soft.divan.financemanager.feature.account.account_impl.domain.usecase.GetAccountByIdUseCase
import javax.inject.Inject

class GetAccountByIdUseCaseImpl @Inject constructor(
    private val accountRepository: AccountRepository
) : GetAccountByIdUseCase {
    override suspend fun invoke(id: String): DomainResult<Account> {
        return accountRepository.getAccountById(id)
    }
}