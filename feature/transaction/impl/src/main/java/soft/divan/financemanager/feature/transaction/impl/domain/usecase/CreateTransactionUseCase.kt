package soft.divan.financemanager.feature.transaction.impl.domain.usecase

import soft.divan.financemanager.core.domain.model.Transaction
import soft.divan.financemanager.core.domain.result.DomainResult

interface CreateTransactionUseCase {
    suspend operator fun invoke(transaction: Transaction): DomainResult<Unit>
}