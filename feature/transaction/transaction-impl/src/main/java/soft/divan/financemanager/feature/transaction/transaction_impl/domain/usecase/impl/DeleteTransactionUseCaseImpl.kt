package soft.divan.financemanager.feature.transaction.transaction_impl.domain.usecase.impl

import soft.divan.financemanager.core.domain.repository.TransactionRepository
import soft.divan.financemanager.core.domain.result.DomainResult
import soft.divan.financemanager.feature.transaction.transaction_impl.domain.usecase.DeleteTransactionUseCase
import javax.inject.Inject

class DeleteTransactionUseCaseImpl @Inject constructor(
    private val transactionRepository: TransactionRepository
) : DeleteTransactionUseCase {
    override suspend fun invoke(transactionId: Int): DomainResult<Unit> {
        return transactionRepository.deleteTransaction(transactionId)
    }
}