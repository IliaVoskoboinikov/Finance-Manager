package soft.divan.financemanager.core.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import soft.divan.financemanager.core.notifications.model.NotificationMessage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelperImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : NotificationHelper {

    /**
     * Канал создаётся лениво перед первым показом, а не в `init`.
     *
     * Создание канала — обращение к системному сервису; в конструкторе синглтона это
     * побочный эффект, который выполнялся бы при построении графа Hilt (в том числе в
     * процессах, где уведомления вообще не показываются) и мешал бы тестам.
     * `createNotificationChannel` идемпотентен, повторный вызов ничего не ломает.
     */
    private fun ensureChannel(manager: NotificationManagerCompat) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_general_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.notification_channel_general_description)
        }
        manager.createNotificationChannel(channel)
    }

    override fun showNotification(message: NotificationMessage) {
        val manager = NotificationManagerCompat.from(context)

        // checkSelfPermission развёрнут здесь, а не спрятан в хелпер, намеренно: так его
        // видит lint-правило MissingPermission — межпроцедурный поток оно не отслеживает.
        // До Android 13 POST_NOTIFICATIONS не является runtime-разрешением, и для
        // объявленного в манифесте разрешения checkSelfPermission вернёт GRANTED;
        // случай «выключено в системных настройках» закрывает areNotificationsEnabled().
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        if (!granted || !manager.areNotificationsEnabled()) return

        ensureChannel(manager)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(message.iconRes ?: context.applicationInfo.icon)
            .setContentTitle(message.title)
            .setContentText(message.body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message.body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        // Лаунч-интент может отсутствовать (например, у приложения без launcher-Activity),
        // тогда показываем уведомление без перехода вместо падения на PendingIntent(null).
        launchPendingIntent()?.let(builder::setContentIntent)

        manager.notify(message.id, builder.build())
    }

    private fun launchPendingIntent(): PendingIntent? {
        val intent = context.packageManager
            .getLaunchIntentForPackage(context.packageName) ?: return null

        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    companion object {
        const val CHANNEL_ID = "finance_manager_general"
    }
}
