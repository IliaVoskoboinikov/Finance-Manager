package soft.divan.financemanager.core.notifications.scheduler

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import io.mockk.mockk
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Test
import soft.divan.financemanager.core.notifications.worker.InactivityWorker
import soft.divan.financemanager.core.workmanager.WORKER_CLASS_NAME
import java.util.concurrent.TimeUnit

class InactivityReminderSchedulerImplTest {

    private val workManager = mockk<WorkManager>(relaxed = true)
    private val scheduler = InactivityReminderSchedulerImpl(workManager)

    @After
    fun tearDown() = unmockkAll()

    private fun capturedRequest(): OneTimeWorkRequest {
        val request = slot<OneTimeWorkRequest>()
        verify {
            workManager.enqueueUniqueWork(
                InactivityWorker.WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                capture(request)
            )
        }
        return request.captured
    }

    @Test
    fun `onUserActive enqueues unique one-time work under the inactivity name`() {
        scheduler.onUserActive()

        verify {
            workManager.enqueueUniqueWork(
                InactivityWorker.WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                any<OneTimeWorkRequest>()
            )
        }
    }

    @Test
    fun `onUserActive uses REPLACE so the timer is re-armed, not kept`() {
        scheduler.onUserActive()

        // KEEP оставил бы первый отсчёт навсегда: таймер обязан сдвигаться
        verify(exactly = 0) {
            workManager.enqueueUniqueWork(
                any<String>(),
                ExistingWorkPolicy.KEEP,
                any<OneTimeWorkRequest>()
            )
        }
    }

    @Test
    fun `scheduled work is delayed by the inactivity threshold`() {
        scheduler.onUserActive()

        val expected = TimeUnit.DAYS.toMillis(INACTIVITY_THRESHOLD_DAYS)
        assertThat(capturedRequest().workSpec.initialDelay).isEqualTo(expected)
    }

    @Test
    fun `scheduled work delegates to InactivityWorker`() {
        scheduler.onUserActive()

        val className = capturedRequest().workSpec.input.getString(WORKER_CLASS_NAME)
        assertThat(className).isEqualTo(InactivityWorker::class.java.name)
    }

    @Test
    fun `repeated activity re-arms the timer each time`() {
        scheduler.onUserActive()
        scheduler.onUserActive()
        scheduler.onUserActive()

        verify(exactly = 3) {
            workManager.enqueueUniqueWork(
                InactivityWorker.WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                any<OneTimeWorkRequest>()
            )
        }
    }

    @Test
    fun `cancel removes the scheduled reminder`() {
        scheduler.cancel()

        verify { workManager.cancelUniqueWork(InactivityWorker.WORK_NAME) }
    }

    @Test
    fun `inactivity threshold is seven days`() {
        assertThat(INACTIVITY_THRESHOLD_DAYS).isEqualTo(7L)
    }
}
