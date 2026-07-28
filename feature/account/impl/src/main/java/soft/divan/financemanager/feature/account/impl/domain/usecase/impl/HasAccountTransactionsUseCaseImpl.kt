package soft.divan.financemanager.feature.account.impl.domain.usecase.impl

import soft.divan.financemanager.core.domain.repository.TransactionRepository
import soft.divan.financemanager.core.domain.result.DomainResult
import soft.divan.financemanager.feature.account.impl.domain.usecase.HasAccountTransactionsUseCase
import javax.inject.Inject

class HasAccountTransactionsUseCaseImpl @Inject constructor(
    private val transactionRepository: TransactionRepository
) : HasAccountTransactionsUseCase {
    override suspend fun invoke(accountId: String): DomainResult<Boolean> =
        transactionRepository.hasTransactions(accountId)
}
