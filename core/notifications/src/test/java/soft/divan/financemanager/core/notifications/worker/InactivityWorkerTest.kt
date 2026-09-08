package soft.divan.financemanager.core.notifications.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import soft.divan.financemanager.core.notifications.NotificationHelper
import soft.divan.financemanager.core.notifications.R
import soft.divan.financemanager.core.notifications.model.NotificationIds
import soft.divan.financemanager.core.notifications.model.NotificationMessage

class InactivityWorkerTest {

    private lateinit var context: Context
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var worker: InactivityWorker

    private val title = "We miss you!"
    private val body = "You haven't opened the app for a week."

    @Before
    fun setUp() {
        context = mockk(relaxed = true)
        notificationHelper = mockk(relaxed = true)

        every { context.getString(R.string.notification_inactivity_title) } returns title
        every { context.getString(R.string.notification_inactivity_message) } returns body

        worker = InactivityWorker(context, mockk<WorkerParameters>(relaxed = true), notificationHelper)
    }

    @Test
    fun `doWork returns success`() = runTest {
        assertThat(worker.doWork()).isEqualTo(ListenableWorker.Result.success())
    }

    @Test
    fun `doWork shows the localized inactivity notification`() = runTest {
        worker.doWork()

        val message = slot<NotificationMessage>()
        verify { notificationHelper.showNotification(capture(message)) }

        assertThat(message.captured.title).isEqualTo(title)
        assertThat(message.captured.body).isEqualTo(body)
    }

    @Test
    fun `doWork uses the dedicated inactivity notification id`() = runTest {
        worker.doWork()

        val message = slot<NotificationMessage>()
        verify { notificationHelper.showNotification(capture(message)) }

        // фиксированный id — повторное напоминание заменяет предыдущее, а не копится
        assertThat(message.captured.id).isEqualTo(NotificationIds.INACTIVITY_REMINDER)
    }

    @Test
    fun `work name is stable`() {
        // имя уникальной работы — часть контракта с планировщиком
        assertThat(InactivityWorker.WORK_NAME).isEqualTo("InactivityNotificationWork")
    }
}
