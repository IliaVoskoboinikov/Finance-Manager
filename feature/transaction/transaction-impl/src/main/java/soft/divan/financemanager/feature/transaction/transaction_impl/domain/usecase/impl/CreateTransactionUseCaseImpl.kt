package soft.divan.financemanager.feature.transaction.transaction_impl.domain.usecase.impl

import soft.divan.financemanager.core.domain.model.Transaction
import soft.divan.financemanager.core.domain.repository.TransactionRepository
import soft.divan.financemanager.feature.transaction.transaction_impl.domain.usecase.CreateTransactionUseCase
import javax.inject.Inject

class CreateTransactionUseCaseImpl @Inject constructor(
    val transactionRepository: TransactionRepository
) : CreateTransactionUseCase {
    override suspend fun invoke(transaction: Transaction): Result<Unit> {
        return transactionRepository.createTransaction(transaction)
    }
}