package soft.divan.financemanager.feature.transaction.transaction_impl.domain.usecase

import soft.divan.financemanager.core.domain.model.Transaction

interface CreateTransactionUseCase {
    suspend operator fun invoke(transaction: Transaction): Result<Transaction>
}