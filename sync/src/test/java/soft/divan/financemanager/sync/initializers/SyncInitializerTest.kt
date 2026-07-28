package soft.divan.financemanager.sync.initializers

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import soft.divan.financemanager.sync.domain.usecase.ObserveSyncIntervalHoursUseCase
import soft.divan.financemanager.sync.scheduler.SyncScheduler
import soft.divan.financemanager.sync.worker.SYNCHRONIZATION_PERIOD_IN_HOURS

class SyncInitializerTest {

    private val observeSyncIntervalHoursUseCase = mockk<ObserveSyncIntervalHoursUseCase>()
    private val syncScheduler = mockk<SyncScheduler>(relaxUnitFun = true)

    private val initializer = SyncInitializer(
        observeSyncIntervalHoursUseCase = observeSyncIntervalHoursUseCase,
        syncScheduler = syncScheduler
    )

    @Test
    fun `initialize schedules one-time sync then periodic with stored interval`() = runTest {
        every { observeSyncIntervalHoursUseCase() } returns flowOf(8)

        initializer.initialize()

        verifyOrder {
            syncScheduler.scheduleOneTimeSync()
            syncScheduler.schedulePeriodicSync(8)
        }
    }

    @Test
    fun `initialize falls back to default interval when nothing stored`() = runTest {
        every { observeSyncIntervalHoursUseCase() } returns flowOf(null)

        initializer.initialize()

        verify(exactly = 1) {
            syncScheduler.schedulePeriodicSync(SYNCHRONIZATION_PERIOD_IN_HOURS)
        }
    }
}
