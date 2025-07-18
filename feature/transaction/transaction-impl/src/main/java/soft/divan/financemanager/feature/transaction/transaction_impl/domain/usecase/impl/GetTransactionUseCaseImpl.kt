package soft.divan.financemanager.feature.transaction.transaction_impl.domain.usecase.impl

import soft.divan.financemanager.core.domain.model.Transaction
import soft.divan.financemanager.feature.transaction.transaction_impl.domain.repository.TransactionRepository
import soft.divan.financemanager.feature.transaction.transaction_impl.domain.usecase.GetTransactionUseCase
import javax.inject.Inject

class GetTransactionUseCaseImpl @Inject constructor(
    private val transactionRepository: TransactionRepository
) : GetTransactionUseCase {
    override suspend fun invoke(id: String): Result<List<Transaction>> {
        return transactionRepository.getTransactions(id)
    }
}
