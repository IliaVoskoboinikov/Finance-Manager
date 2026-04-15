package soft.divan.financemanager.core.notifications.fcm

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import soft.divan.financemanager.core.notifications.NotificationHelper
import soft.divan.financemanager.core.notifications.model.NotificationMessage
import javax.inject.Inject

@AndroidEntryPoint
class FinanceFcmService : FirebaseMessagingService() {

    @Inject
    lateinit var notificationHelper: NotificationHelper

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val title = message.notification?.title ?: message.data["title"] ?: "Finance Manager"
        val body = message.notification?.body ?: message.data["body"] ?: ""

        if (body.isNotEmpty()) {
            notificationHelper.showNotification(
                NotificationMessage(
                    title = title,
                    body = body
                )
            )
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }
}
