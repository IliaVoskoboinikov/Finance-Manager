package soft.divan.financemanager.feature.transaction.impl.domain.usecase.impl

import soft.divan.financemanager.core.domain.model.Transaction
import soft.divan.financemanager.core.domain.repository.TransactionRepository
import soft.divan.financemanager.core.domain.result.DomainResult
import soft.divan.financemanager.feature.transaction.impl.domain.usecase.GetTransactionUseCase
import javax.inject.Inject

class GetTransactionUseCaseImpl @Inject constructor(
    private val transactionRepository: TransactionRepository
) : GetTransactionUseCase {
    override suspend fun invoke(id: String): DomainResult<Transaction> {
        return transactionRepository.getById(id)
    }
}
