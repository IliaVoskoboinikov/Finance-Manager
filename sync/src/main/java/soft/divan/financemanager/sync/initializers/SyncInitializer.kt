package soft.divan.financemanager.sync.initializers

import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import soft.divan.financemanager.core.domain.model.LocalDataState
import soft.divan.financemanager.core.domain.usecase.ObserveLocalDataStateUseCase
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
    private val observeLocalDataState: ObserveLocalDataStateUseCase,
    private val syncScheduler: SyncScheduler
) {

    /**
     * Планирует фоновую синхронизацию и запускает немедленную каждый раз, когда локальные данные
     * становятся доступны.
     *
     * На уровнях без PIN это случается один раз — при старте. На уровне PIN база открывается
     * только после PIN или биометрии, а фоновые прогоны её пропускают, поэтому синк при каждой
     * разблокировке — единственный, который реально что-то отправит.
     *
     * Не завершается: следит за состоянием данных всё время жизни процесса.
     */
    suspend fun initialize() {
        val interval = observeSyncIntervalHoursUseCase().first() ?: SYNCHRONIZATION_PERIOD_IN_HOURS
        // фоновая периодическая
        syncScheduler.schedulePeriodicSync(interval)

        observeLocalDataState()
            .map { it is LocalDataState.Open }
            .distinctUntilChanged()
            .filter { isOpen -> isOpen }
            .collect { syncScheduler.scheduleOneTimeSync() }
    }
}
