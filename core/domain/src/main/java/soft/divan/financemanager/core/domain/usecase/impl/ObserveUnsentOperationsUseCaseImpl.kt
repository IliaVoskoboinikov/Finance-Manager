package soft.divan.financemanager.core.domain.usecase.impl

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.core.domain.repository.OutboxRepository
import soft.divan.financemanager.core.domain.result.DomainResult
import soft.divan.financemanager.core.domain.usecase.ObserveUnsentOperationsUseCase
import javax.inject.Inject

class ObserveUnsentOperationsUseCaseImpl @Inject constructor(
    private val outboxRepository: OutboxRepository
) : ObserveUnsentOperationsUseCase {
    override fun invoke(): Flow<DomainResult<Int>> = outboxRepository.observeFailedCount()
}
