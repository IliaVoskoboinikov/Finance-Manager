package soft.divan.financemanager.core.notifications.fcm

import com.google.firebase.messaging.RemoteMessage
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import soft.divan.financemanager.core.notifications.NotificationHelper
import soft.divan.financemanager.core.notifications.model.NotificationMessage

class FinanceFcmServiceTest {

    private lateinit var notificationHelper: NotificationHelper
    private lateinit var service: FinanceFcmService

    @Before
    fun setUp() {
        notificationHelper = mockk(relaxed = true)
        service = FinanceFcmService().apply {
            this.notificationHelper = this@FinanceFcmServiceTest.notificationHelper
        }
    }

    @Test
    fun `onMessageReceived with notification should show notification`() {
        // Given
        val title = "Test Title"
        val body = "Test Body"
        val remoteMessage = mockk<RemoteMessage>(relaxed = true)
        val notification = mockk<RemoteMessage.Notification>(relaxed = true)

        io.mockk.every { remoteMessage.notification } returns notification
        io.mockk.every { notification.title } returns title
        io.mockk.every { notification.body } returns body
        io.mockk.every { remoteMessage.data } returns emptyMap()

        // When
        service.onMessageReceived(remoteMessage)

        // Then
        verify {
            notificationHelper.showNotification(
                match {
                    it.title == title && it.body == body
                }
            )
        }
    }

    @Test
    fun `onMessageReceived with data should show notification`() {
        // Given
        val title = "Data Title"
        val body = "Data Body"
        val remoteMessage = mockk<RemoteMessage>(relaxed = true)

        io.mockk.every { remoteMessage.notification } returns null
        io.mockk.every { remoteMessage.data } returns mapOf(
            "title" to title,
            "body" to body
        )

        // When
        service.onMessageReceived(remoteMessage)

        // Then
        verify {
            notificationHelper.showNotification(
                match {
                    it.title == title && it.body == body
                }
            )
        }
    }

    @Test
    fun `onMessageReceived with empty body should not show notification`() {
        // Given
        val remoteMessage = mockk<RemoteMessage>(relaxed = true)

        io.mockk.every { remoteMessage.notification } returns null
        io.mockk.every { remoteMessage.data } returns emptyMap()

        // When
        service.onMessageReceived(remoteMessage)

        // Then
        verify(exactly = 0) {
            notificationHelper.showNotification(any())
        }
    }
}
