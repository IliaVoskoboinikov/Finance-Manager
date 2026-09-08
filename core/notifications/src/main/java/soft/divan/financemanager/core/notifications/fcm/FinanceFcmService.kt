package soft.divan.financemanager.core.notifications.fcm

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import soft.divan.financemanager.core.notifications.NotificationHelper
import soft.divan.financemanager.core.notifications.R
import soft.divan.financemanager.core.notifications.model.NotificationIds
import soft.divan.financemanager.core.notifications.model.NotificationMessage
import javax.inject.Inject

/**
 * Приём пушей.
 *
 * `onMessageReceived` вызывается системой для data-сообщений всегда, а для
 * notification-сообщений — только когда приложение на переднем плане (иначе уведомление
 * рисует сам SDK). Поэтому читаем и `notification`, и `data`.
 */
@AndroidEntryPoint
class FinanceFcmService : FirebaseMessagingService() {

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var pushSubscriptionManager: PushSubscriptionManager

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val body = message.notification?.body ?: message.data[KEY_BODY].orEmpty()
        // Пуш без текста показывать нечем — молча игнорируем.
        if (body.isBlank()) return

        val title = message.notification?.title
            ?: message.data[KEY_TITLE]
            ?: getString(R.string.notification_push_default_title)

        notificationHelper.showNotification(
            NotificationMessage(
                // Разные пуши не должны затирать друг друга в шторке, поэтому id
                // выводим из messageId, а не берём константу.
                id = message.messageId?.hashCode() ?: NotificationIds.PUSH_FALLBACK,
                title = title,
                body = body
            )
        )
    }

    /**
     * Токен меняется при переустановке, очистке данных и ротации со стороны FCM.
     * Подписка на topic привязана к токену, поэтому её нужно возобновлять.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        pushSubscriptionManager.subscribeToBroadcasts()
    }

    private companion object {
        const val KEY_TITLE = "title"
        const val KEY_BODY = "body"
    }
}
