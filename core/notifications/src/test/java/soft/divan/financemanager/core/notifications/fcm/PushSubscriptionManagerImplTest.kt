package soft.divan.financemanager.core.notifications.fcm

import com.google.firebase.messaging.FirebaseMessaging
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class PushSubscriptionManagerImplTest {

    private val firebaseMessaging = mockk<FirebaseMessaging>(relaxed = true)
    private val manager = PushSubscriptionManagerImpl({ firebaseMessaging })

    @Test
    fun `subscribes to the broadcast topic`() {
        manager.subscribeToBroadcasts()

        verify {
            firebaseMessaging.subscribeToTopic(PushSubscriptionManagerImpl.BROADCAST_TOPIC)
        }
    }

    @Test
    fun `repeated subscription is delegated to the sdk each time`() {
        manager.subscribeToBroadcasts()
        manager.subscribeToBroadcasts()

        // идемпотентность обеспечивает сам FCM SDK, менеджер не кеширует состояние
        verify(exactly = 2) {
            firebaseMessaging.subscribeToTopic(PushSubscriptionManagerImpl.BROADCAST_TOPIC)
        }
    }

    @Test
    fun `firebase sdk is not touched until a subscription is requested`() {
        var resolved = false
        PushSubscriptionManagerImpl {
            resolved = true
            firebaseMessaging
        }

        // FirebaseMessaging.getInstance() требует поднятого FirebaseApp: если его дёрнуть
        // при сборке Hilt-графа, падает любой процесс без инициализированного Firebase
        assertThat(resolved).isFalse()
    }

    @Test
    fun `broadcast topic name is stable`() {
        // имя topic — контракт с бэкендом и Firebase Console
        assertThat(PushSubscriptionManagerImpl.BROADCAST_TOPIC).isEqualTo("all")
    }
}
