package soft.divan.financemanager.core.domain.usecase.impl

import soft.divan.financemanager.core.domain.repository.OutboxRepository
import soft.divan.financemanager.core.domain.result.DomainResult
import soft.divan.financemanager.core.domain.usecase.RetryUnsentOperationsUseCase
import javax.inject.Inject

class RetryUnsentOperationsUseCaseImpl @Inject constructor(
    private val outboxRepository: OutboxRepository
) : RetryUnsentOperationsUseCase {
    override suspend fun invoke(): DomainResult<Unit> = outboxRepository.retryFailed()
}
