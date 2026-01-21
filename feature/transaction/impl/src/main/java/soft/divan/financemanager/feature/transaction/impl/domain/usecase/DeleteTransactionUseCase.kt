package soft.divan.financemanager.feature.transaction.impl.domain.usecase

import soft.divan.financemanager.core.domain.result.DomainResult

interface DeleteTransactionUseCase {
    suspend operator fun invoke(id: String): DomainResult<Unit>
}