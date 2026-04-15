package soft.divan.financemanager.core.notifications

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ResolveInfo
import androidx.test.core.app.ApplicationProvider
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowNotificationManager
import soft.divan.financemanager.core.notifications.model.NotificationMessage

@RunWith(RobolectricTestRunner::class)
class NotificationHelperImplTest {

    private lateinit var context: Context
    private lateinit var notificationManager: NotificationManager
    private lateinit var shadowNotificationManager: ShadowNotificationManager
    private lateinit var notificationHelper: NotificationHelperImpl
    private lateinit var firebaseMessaging: FirebaseMessaging

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        notificationManager = context.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager
        shadowNotificationManager = Shadows.shadowOf(notificationManager)

        firebaseMessaging = mockk(relaxed = true)

        // Trigger static initialization before mockkStatic to avoid "Recorded calls count differ"
        FirebaseMessaging::class.java
        mockkStatic(FirebaseMessaging::class)
        every { FirebaseMessaging.getInstance() } returns firebaseMessaging

        // We do NOT mock PendingIntent statically because Robolectric provides a functional implementation.
        // This avoids the MockKException related to static initializers.

        notificationHelper = NotificationHelperImpl(context)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `showNotification should post notification to NotificationManager`() {
        // Given
        val message = NotificationMessage(
            id = 1,
            title = "Test Title",
            body = "Test Body"
        )

        // Setup ShadowPackageManager to return a launch intent
        val launchIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            setPackage(context.packageName)
        }
        val resolveInfo = ResolveInfo().apply {
            activityInfo = ActivityInfo().apply {
                packageName = context.packageName
                name = "MainActivity"
            }
        }
        Shadows.shadowOf(context.packageManager).addResolveInfoForIntent(launchIntent, resolveInfo)

        // When
        notificationHelper.showNotification(message)

        // Then
        val notifications = shadowNotificationManager.allNotifications
        assertThat(notifications).hasSize(1)

        val notification = notifications[0]
        assertThat(
            notification.extras.getCharSequence("android.title").toString()
        ).isEqualTo("Test Title")
        assertThat(notification.extras.getCharSequence("android.text").toString()).isEqualTo("Test Body")
    }

    @Test
    fun `getFcmToken should return token on success`() {
        // Given
        val token = "test_token"
        val task = mockk<Task<String>>()
        every { firebaseMessaging.token } returns task

        val slot = slot<OnCompleteListener<String>>()
        every { task.addOnCompleteListener(capture(slot)) } returns task
        every { task.isSuccessful } returns true
        every { task.result } returns token

        var capturedToken: String? = null

        // When
        notificationHelper.getFcmToken { capturedToken = it }
        slot.captured.onComplete(task)

        // Then
        assertThat(capturedToken).isEqualTo(token)
    }

    @Test
    fun `getFcmToken should return null on failure`() {
        // Given
        val task = mockk<Task<String>>()
        every { firebaseMessaging.token } returns task

        val slot = slot<OnCompleteListener<String>>()
        every { task.addOnCompleteListener(capture(slot)) } returns task
        every { task.isSuccessful } returns false

        var capturedToken: String? = "not null"

        // When
        notificationHelper.getFcmToken { capturedToken = it }
        slot.captured.onComplete(task)

        // Then
        assertThat(capturedToken).isNull()
    }
}
