package soft.divan.financemanager.sync.domain.usecase.impl

import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import soft.divan.financemanager.sync.domain.repository.SyncRepository
import soft.divan.financemanager.sync.scheduler.SyncScheduler
import soft.divan.financemanager.sync.worker.MAX_SYNC_INTERVAL_HOURS
import soft.divan.financemanager.sync.worker.MIN_SYNC_INTERVAL_HOURS

class SetSyncIntervalHoursUseCaseImplTest {

    private val repository = mockk<SyncRepository>(relaxed = true)
    private val scheduler = mockk<SyncScheduler>(relaxed = true)
    private val useCase = SetSyncIntervalHoursUseCaseImpl(repository, scheduler)

    @Test
    fun `passes through a value inside the allowed range`() = runTest {
        useCase(6)

        coVerify { repository.setSyncIntervalHours(6) }
        coVerify { scheduler.schedulePeriodicSync(6) }
    }

    @Test
    fun `clamps zero and negative values up to the minimum`() = runTest {
        useCase(0)
        useCase(-5)

        coVerify(exactly = 2) { repository.setSyncIntervalHours(MIN_SYNC_INTERVAL_HOURS) }
        coVerify(exactly = 2) { scheduler.schedulePeriodicSync(MIN_SYNC_INTERVAL_HOURS) }
    }

    @Test
    fun `clamps too large values down to the maximum`() = runTest {
        useCase(1000)

        coVerify { repository.setSyncIntervalHours(MAX_SYNC_INTERVAL_HOURS) }
        coVerify { scheduler.schedulePeriodicSync(MAX_SYNC_INTERVAL_HOURS) }
    }
}
