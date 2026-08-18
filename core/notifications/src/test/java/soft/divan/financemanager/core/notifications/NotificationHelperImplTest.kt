package soft.divan.financemanager.core.notifications

import android.Manifest
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import soft.divan.financemanager.core.notifications.model.NotificationMessage
import org.robolectric.shadows.ShadowNotificationManager

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU])
class NotificationHelperImplTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val helper = NotificationHelperImpl(context)

    private val notificationManager: NotificationManager
        get() = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val shadowManager: ShadowNotificationManager
        get() = shadowOf(notificationManager)

    private fun grantPostPermission() {
        shadowOf(ApplicationProvider.getApplicationContext<android.app.Application>())
            .grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
    }

    private fun message(id: Int = 42, body: String = "body") = NotificationMessage(
        id = id,
        title = "title",
        body = body
    )

    @Test
    fun `constructing the helper does not touch the notification manager`() {
        // Канал создаётся лениво — построение графа Hilt не должно дёргать системный сервис
        NotificationHelperImpl(context)

        assertThat(shadowManager.notificationChannels).isEmpty()
    }

    @Test
    fun `shows notification and registers channel when permission granted`() {
        grantPostPermission()

        helper.showNotification(message())

        val posted = shadowManager.allNotifications
        assertThat(posted).hasSize(1)
        assertThat(shadowManager.notificationChannels.map { it.id })
            .contains(NotificationHelperImpl.CHANNEL_ID)
    }

    @Test
    fun `does not show notification when POST_NOTIFICATIONS is denied on API 33+`() {
        // разрешение намеренно не выдаём
        assertThat(
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
        ).isEqualTo(PackageManager.PERMISSION_DENIED)

        helper.showNotification(message())

        assertThat(shadowManager.allNotifications).isEmpty()
    }

    @Test
    fun `notification with the same id replaces the previous one`() {
        grantPostPermission()

        helper.showNotification(message(id = 7, body = "first"))
        helper.showNotification(message(id = 7, body = "second"))

        assertThat(shadowManager.allNotifications).hasSize(1)
    }

    @Test
    fun `notifications with different ids coexist`() {
        grantPostPermission()

        helper.showNotification(message(id = 1))
        helper.showNotification(message(id = 2))

        assertThat(shadowManager.allNotifications).hasSize(2)
    }

    @Test
    fun `blank-safe - body is rendered into the notification`() {
        grantPostPermission()

        helper.showNotification(message(body = "hello"))

        val notification = shadowManager.allNotifications.single()
        assertThat(shadowOf(notification).contentText).isEqualTo("hello")
    }

    @Test
    fun `notification opens the app when a launcher activity exists`() {
        grantPostPermission()
        registerLauncherActivity()

        helper.showNotification(message())

        assertThat(shadowManager.allNotifications.single().contentIntent).isNotNull()
    }

    @Test
    fun `notification is still shown when the app has no launcher activity`() {
        grantPostPermission()
        // launcher-активность намеренно не регистрируем: getLaunchIntentForPackage вернёт
        // null, и уведомление должно показаться без перехода, а не упасть
        helper.showNotification(message())

        assertThat(shadowManager.allNotifications.single().contentIntent).isNull()
    }

    @Test
    fun `custom icon overrides the application icon`() {
        grantPostPermission()

        helper.showNotification(
            NotificationMessage(
                id = 5,
                title = "t",
                body = "b",
                iconRes = android.R.drawable.ic_dialog_info
            )
        )

        val notification = shadowManager.allNotifications.single()
        assertThat(notification.smallIcon.resId).isEqualTo(android.R.drawable.ic_dialog_info)
    }

    private fun registerLauncherActivity() {
        val component = ComponentName(context.packageName, "${context.packageName}.Launcher")
        val shadowPackageManager = shadowOf(context.packageManager)
        shadowPackageManager.addActivityIfNotPresent(component)
        shadowPackageManager.addIntentFilterForActivity(
            component,
            IntentFilter(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_LAUNCHER) }
        )
    }
}
