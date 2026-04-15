package soft.divan.financemanager.core.notifications

import android.content.Context
import android.graphics.ColorSpace.match
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import soft.divan.financemanager.core.notifications.model.NotificationMessage
import soft.divan.financemanager.core.notifications.worker.InactivityWorker

class InactivityWorkerTest {

    private lateinit var context: Context
    private lateinit var workerParams: WorkerParameters
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var worker: InactivityWorker

    @Before
    fun setUp() {
        context = mockk(relaxed = true)
        workerParams = mockk(relaxed = true)
        notificationHelper = mockk(relaxed = true)
        worker = InactivityWorker(context, workerParams, notificationHelper)
    }

    @Test
    fun `doWork should show notification and return success`() = runBlocking {
        // Given
        val title = "Мы скучаем!"
        val message = "Вы не заходили в приложение уже целую неделю. Самое время проверить свои финансы!"

        every { context.getString(R.string.notification_inactivity_title) } returns title
        every { context.getString(R.string.notification_inactivity_message) } returns message

        // When
        val result = worker.doWork()

        // Then
        assertThat(result).isEqualTo(ListenableWorker.Result.success())
        verify {
            notificationHelper.showNotification(
                match {
                    it.title == title && it.body == message
                }
            )
        }
    }
}
