package soft.divan.financemanager.feature.transaction.transaction_impl.domain.usecase

import soft.divan.financemanager.core.domain.model.Transaction

interface GetTransactionUseCase {
    suspend operator fun invoke(id: String): Result<List<Transaction>>
}