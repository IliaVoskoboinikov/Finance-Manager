package soft.divan.financemanager.sync.domain.usecase.impl

import soft.divan.financemanager.sync.domain.repository.SyncRepository
import soft.divan.financemanager.sync.domain.usecase.SetSyncIntervalHoursUseCase
import soft.divan.financemanager.sync.scheduler.SyncScheduler
import soft.divan.financemanager.sync.worker.MAX_SYNC_INTERVAL_HOURS
import soft.divan.financemanager.sync.worker.MIN_SYNC_INTERVAL_HOURS
import javax.inject.Inject

class SetSyncIntervalHoursUseCaseImpl @Inject constructor(
    private val repository: SyncRepository,
    private val syncScheduler: SyncScheduler
) : SetSyncIntervalHoursUseCase {
    /**
     * Значение зажимается в допустимый диапазон, чтобы ни сохранённый интервал, ни
     * запланированная задача WorkManager не оказались некорректными (0/отрицательное/
     * слишком большое), независимо от источника вызова.
     */
    override suspend fun invoke(hours: Int) {
        val safeHours = hours.coerceIn(MIN_SYNC_INTERVAL_HOURS, MAX_SYNC_INTERVAL_HOURS)
        repository.setSyncIntervalHours(safeHours)
        syncScheduler.schedulePeriodicSync(safeHours)
    }
}
