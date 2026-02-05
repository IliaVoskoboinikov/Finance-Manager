package soft.divan.financemanager.feature.account.impl.domain.usecase

import soft.divan.financemanager.core.domain.result.DomainResult

interface DeleteAccountUseCase {
    suspend operator fun invoke(id: String): DomainResult<Unit>
}
