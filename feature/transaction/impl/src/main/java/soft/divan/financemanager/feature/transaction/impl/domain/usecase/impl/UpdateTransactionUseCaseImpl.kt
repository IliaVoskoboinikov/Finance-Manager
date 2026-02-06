package soft.divan.financemanager.feature.transaction.impl.domain.usecase.impl

import soft.divan.financemanager.core.domain.model.Transaction
import soft.divan.financemanager.core.domain.repository.TransactionRepository
import soft.divan.financemanager.core.domain.result.DomainResult
import soft.divan.financemanager.feature.transaction.impl.domain.usecase.UpdateTransactionUseCase
import javax.inject.Inject

class UpdateTransactionUseCaseImpl @Inject constructor(
    val transactionRepository: TransactionRepository
) : UpdateTransactionUseCase {
    override suspend fun invoke(transaction: Transaction): DomainResult<Unit> {
        return transactionRepository.update(transaction)
    }
}
// Revue me>>
