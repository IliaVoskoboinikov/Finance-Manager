package soft.divan.financemanager.sync.initializers

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test
import soft.divan.financemanager.core.domain.model.LocalDataState
import soft.divan.financemanager.core.domain.usecase.ObserveLocalDataStateUseCase
import soft.divan.financemanager.sync.domain.usecase.ObserveSyncIntervalHoursUseCase
import soft.divan.financemanager.sync.scheduler.SyncScheduler
import soft.divan.financemanager.sync.worker.SYNCHRONIZATION_PERIOD_IN_HOURS

class SyncInitializerTest {

    private val observeSyncIntervalHoursUseCase = mockk<ObserveSyncIntervalHoursUseCase>()
    private val syncScheduler = mockk<SyncScheduler>(relaxUnitFun = true)
    private val dataState = MutableStateFlow<LocalDataState>(LocalDataState.Initializing)

    private val initializer = SyncInitializer(
        observeSyncIntervalHoursUseCase = observeSyncIntervalHoursUseCase,
        observeLocalDataState = object : ObserveLocalDataStateUseCase {
            override fun invoke(): StateFlow<LocalDataState> = dataState
        },
        syncScheduler = syncScheduler
    )

    @Test
    fun `periodic sync is scheduled with the stored interval right away`() = runTest {
        every { observeSyncIntervalHoursUseCase() } returns flowOf(8)

        val job = launch { initializer.initialize() }
        runCurrent()

        verify(exactly = 1) { syncScheduler.schedulePeriodicSync(8) }
        // Данные ещё не открыты — немедленный синк ждёт
        verify(exactly = 0) { syncScheduler.scheduleOneTimeSync() }
        job.cancel()
    }

    @Test
    fun `default interval is used when nothing is stored`() = runTest {
        every { observeSyncIntervalHoursUseCase() } returns flowOf(null)

        val job = launch { initializer.initialize() }
        runCurrent()

        verify(exactly = 1) { syncScheduler.schedulePeriodicSync(SYNCHRONIZATION_PERIOD_IN_HOURS) }
        job.cancel()
    }

    @Test
    fun `every unlock of the data starts an immediate sync`() = runTest {
        every { observeSyncIntervalHoursUseCase() } returns flowOf(8)
        val job = launch { initializer.initialize() }

        dataState.value = LocalDataState.Open
        runCurrent()
        dataState.value = LocalDataState.Locked(biometricEnabled = false)
        runCurrent()
        dataState.value = LocalDataState.Open
        runCurrent()

        verify(exactly = 2) { syncScheduler.scheduleOneTimeSync() }
        job.cancel()
    }

    @Test
    fun `lost key does not start a sync`() = runTest {
        every { observeSyncIntervalHoursUseCase() } returns flowOf(8)
        val job = launch { initializer.initialize() }

        dataState.value = LocalDataState.KeyLost
        runCurrent()

        verify(exactly = 0) { syncScheduler.scheduleOneTimeSync() }
        job.cancel()
    }
}
