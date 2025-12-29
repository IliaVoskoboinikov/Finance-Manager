package soft.divan.financemanager.feature.synchronization.synchronization_impl.domain.usecase.impl

import soft.divan.financemanager.feature.synchronization.synchronization_impl.domain.usecase.SetSyncIntervalHoursUseCase
import soft.divan.financemanager.sync.domain.repository.SyncRepository
import soft.divan.financemanager.sync.scheduler.SyncScheduler
import javax.inject.Inject

class SetSyncIntervalHoursUseCaseImpl @Inject constructor(
    private val repository: SyncRepository,
    private val syncScheduler: SyncScheduler
) : SetSyncIntervalHoursUseCase {
    override suspend fun invoke(hours: Int) {
        repository.setSyncIntervalHours(hours)
        syncScheduler.schedulePeriodicSync(hours)
    }
}