package soft.divan.financemanager.core.notifications.fcm

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.firebase.messaging.RemoteMessage
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.util.ReflectionHelpers
import org.robolectric.util.ReflectionHelpers.ClassParameter
import soft.divan.financemanager.core.notifications.NotificationHelper
import soft.divan.financemanager.core.notifications.model.NotificationIds
import soft.divan.financemanager.core.notifications.model.NotificationMessage

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class FinanceFcmServiceTest {

    private lateinit var notificationHelper: NotificationHelper
    private lateinit var pushSubscriptionManager: PushSubscriptionManager
    private lateinit var service: FinanceFcmService

    @Before
    fun setUp() {
        notificationHelper = mockk(relaxed = true)
        pushSubscriptionManager = mockk(relaxed = true)

        service = FinanceFcmService().apply {
            notificationHelper = this@FinanceFcmServiceTest.notificationHelper
            pushSubscriptionManager = this@FinanceFcmServiceTest.pushSubscriptionManager
        }

        // Сервис создаём напрямую, а не через Robolectric.setupService: @AndroidEntryPoint
        // дёрнул бы Hilt-инъекцию в onCreate. Нужен только базовый контекст для getString().
        ReflectionHelpers.callInstanceMethod<Void>(
            service,
            "attachBaseContext",
            ClassParameter.from(
                Context::class.java,
                ApplicationProvider.getApplicationContext<Context>()
            )
        )
    }

    private fun remoteMessage(
        notificationTitle: String? = null,
        notificationBody: String? = null,
        data: Map<String, String> = emptyMap(),
        messageId: String? = null
    ): RemoteMessage {
        val notification = if (notificationTitle != null || notificationBody != null) {
            mockk<RemoteMessage.Notification> {
                every { title } returns notificationTitle
                every { body } returns notificationBody
            }
        } else {
            null
        }

        return mockk {
            every { this@mockk.notification } returns notification
            every { this@mockk.data } returns data
            every { this@mockk.messageId } returns messageId
        }
    }

    private fun capturedMessage(): NotificationMessage {
        val message = slot<NotificationMessage>()
        verify { notificationHelper.showNotification(capture(message)) }
        return message.captured
    }

    @Test
    fun `shows notification from the notification payload`() {
        service.onMessageReceived(remoteMessage("Title", "Body"))

        assertThat(capturedMessage().title).isEqualTo("Title")
        assertThat(capturedMessage().body).isEqualTo("Body")
    }

    @Test
    fun `falls back to the data payload when notification block is absent`() {
        service.onMessageReceived(
            remoteMessage(data = mapOf("title" to "DataTitle", "body" to "DataBody"))
        )

        assertThat(capturedMessage().title).isEqualTo("DataTitle")
        assertThat(capturedMessage().body).isEqualTo("DataBody")
    }

    @Test
    fun `uses the default title when the push carries only a body`() {
        service.onMessageReceived(remoteMessage(notificationBody = "Body only"))

        assertThat(capturedMessage().title).isEqualTo("Finance Manager")
    }

    @Test
    fun `ignores a push without body`() {
        service.onMessageReceived(remoteMessage(notificationTitle = "Title"))

        verify(exactly = 0) { notificationHelper.showNotification(any()) }
    }

    @Test
    fun `ignores a push with blank body`() {
        service.onMessageReceived(remoteMessage(notificationTitle = "T", notificationBody = "   "))

        verify(exactly = 0) { notificationHelper.showNotification(any()) }
    }

    @Test
    fun `derives notification id from messageId so pushes do not overwrite each other`() {
        service.onMessageReceived(remoteMessage(notificationBody = "Body", messageId = "abc"))

        assertThat(capturedMessage().id).isEqualTo("abc".hashCode())
    }

    @Test
    fun `falls back to a constant id when messageId is missing`() {
        service.onMessageReceived(remoteMessage(notificationBody = "Body", messageId = null))

        assertThat(capturedMessage().id).isEqualTo(NotificationIds.PUSH_FALLBACK)
    }

    @Test
    fun `re-subscribes to broadcasts when the token rotates`() {
        service.onNewToken("new-token")

        verify { pushSubscriptionManager.subscribeToBroadcasts() }
    }
}
