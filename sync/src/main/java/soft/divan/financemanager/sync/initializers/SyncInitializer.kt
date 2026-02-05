package soft.divan.financemanager.sync.initializers

import kotlinx.coroutines.flow.first
import soft.divan.financemanager.sync.domain.usecase.ObserveSyncIntervalHoursUseCase
import soft.divan.financemanager.sync.scheduler.SyncScheduler
import soft.divan.financemanager.sync.worker.SYNCHRONIZATION_PERIOD_IN_HOURS
import javax.inject.Inject
import javax.inject.Singleton

const val SYNC_ONE_TIME_WORK = "SyncOneTimeWork"
const val SYNC_PERIODIC_WORK = "SyncPeriodicWork"

@Singleton
class SyncInitializer @Inject constructor(
    private val observeSyncIntervalHoursUseCase: ObserveSyncIntervalHoursUseCase,
    private val syncScheduler: SyncScheduler
) {

    suspend fun initialize() {
        val interval = observeSyncIntervalHoursUseCase().first() ?: SYNCHRONIZATION_PERIOD_IN_HOURS
        // немедленная синхронизация при запуске приложения
        syncScheduler.scheduleOneTimeSync()
        // фоновая периодическая
        syncScheduler.schedulePeriodicSync(interval)
    }
}
