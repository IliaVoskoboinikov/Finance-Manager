package soft.divan.financemanager.feature.transaction.transaction_impl.domain.usecase

import soft.divan.financemanager.core.domain.model.Transaction
import soft.divan.financemanager.core.domain.result.DomainResult

interface GetTransactionUseCase {
    suspend operator fun invoke(id: String): DomainResult<Transaction>
}