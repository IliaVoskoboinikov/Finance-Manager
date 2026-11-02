package soft.divan.financemanager.core.notifications.fcm

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

/**
 * Подписка устройства на широковещательные рассылки.
 *
 * Приложению нужны пуши «всем пользователям», и topic — это способ отправлять их
 * **со стороны бэкенда**: один запрос FCM HTTP v1 на `/topics/all` вместо фан-аута по
 * токенам и без хранения базы устройств. Firebase Console умеет слать всем и без topic
 * (адресует по app ID), так что подписка нужна именно для серверных рассылок.
 *
 * Адресная доставка на конкретное устройство — отдельный сценарий, он не реализован:
 * см. `docs/notifications.md`.
 */
interface PushSubscriptionManager {

    /** Подписывает устройство на общий topic рассылок. Идемпотентно. */
    fun subscribeToBroadcasts()
}

/**
 * [FirebaseMessaging] приходит через [Provider], а не напрямую: `getInstance()` требует
 * поднятого `FirebaseApp`, и при прямом внедрении SDK дёргался бы уже в момент сборки
 * Hilt-графа. Это роняло бы любой процесс без инициализированного Firebase — в том числе
 * Robolectric-тесты, которые поднимают граф целиком. С [Provider] обращение к SDK
 * происходит только в момент реальной подписки.
 *
 * Если `FirebaseApp` не поднят и в момент подписки (сборка без `google-services.json`:
 * свежий клон, PR из форка), подписка пропускается: пушей в такой сборке нет, но приложение
 * запускается.
 */
@Singleton
class PushSubscriptionManagerImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val firebaseMessaging: Provider<FirebaseMessaging>
) : PushSubscriptionManager {

    override fun subscribeToBroadcasts() {
        if (FirebaseApp.getApps(context).isEmpty()) return
        firebaseMessaging.get().subscribeToTopic(BROADCAST_TOPIC)
    }

    companion object {
        /** Topic, на который уходит рассылка «всем пользователям». */
        const val BROADCAST_TOPIC = "all"
    }
}
