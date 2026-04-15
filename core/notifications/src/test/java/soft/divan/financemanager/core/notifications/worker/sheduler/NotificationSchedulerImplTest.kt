package soft.divan.financemanager.core.notifications.worker.sheduler

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import soft.divan.financemanager.core.notifications.worker.InactivityWorker

class NotificationSchedulerImplTest {

    private lateinit var context: Context
    private lateinit var workManager: WorkManager
    private lateinit var scheduler: NotificationSchedulerImpl

    @Before
    fun setUp() {
        context = mockk(relaxed = true)
        // Explicitly mock getApplicationContext to avoid AbstractMethodError
        every { context.applicationContext } returns context

        workManager = mockk(relaxed = true)

        // Now passing workManager directly to the constructor as it's injected now
        scheduler = NotificationSchedulerImpl(workManager)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `scheduleInactivityNotification should enqueue unique periodic work`() {
        // When
        scheduler.scheduleInactivityNotification()

        // Then
        verify {
            workManager.enqueueUniquePeriodicWork(
                eq(InactivityWorker.WORK_NAME),
                eq(ExistingPeriodicWorkPolicy.REPLACE),
                any()
            )
        }
    }
}
