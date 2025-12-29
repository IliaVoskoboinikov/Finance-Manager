package soft.divan.financemanager.sync.domain.usecase.impl

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.sync.domain.repository.SyncRepository
import soft.divan.financemanager.sync.domain.usecase.ObserveSyncIntervalHoursUseCase
import javax.inject.Inject

class ObserveSyncIntervalHoursUseCaseImpl @Inject constructor(
    private val repository: SyncRepository,
) : ObserveSyncIntervalHoursUseCase {
    override fun invoke(): Flow<Int?> = repository.observeSyncIntervalHours()
}