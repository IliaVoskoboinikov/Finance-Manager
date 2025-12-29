package soft.divan.financemanager.feature.transaction.transaction_impl.domain.usecase

import soft.divan.financemanager.core.domain.result.DomainResult

interface DeleteTransactionUseCase {
    suspend operator fun invoke(transactionId: Int): DomainResult<Unit>
}