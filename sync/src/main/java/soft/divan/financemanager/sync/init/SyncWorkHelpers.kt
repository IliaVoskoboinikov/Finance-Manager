package soft.divan.financemanager.sync.init

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.Constraints
import androidx.work.ForegroundInfo
import androidx.work.NetworkType


private const val SYNC_NOTIFICATION_ID = 0
private const val SYNC_NOTIFICATION_CHANNEL_ID = "SyncNotificationChannel"

val SyncConstraints
    get() = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()


fun Context.syncForegroundInfo() = ForegroundInfo(
    SYNC_NOTIFICATION_ID,
    syncWorkNotification(),
)

private fun Context.syncWorkNotification(): Notification {
    val channel = NotificationChannel(
        SYNC_NOTIFICATION_CHANNEL_ID,
        "sync",
        NotificationManager.IMPORTANCE_DEFAULT,
    ).apply {
        description = "Sync"
    }
    val notificationManager: NotificationManager? =
        getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

    notificationManager?.createNotificationChannel(channel)

    return NotificationCompat.Builder(
        this,
        SYNC_NOTIFICATION_CHANNEL_ID,
    )
        .setContentTitle("Sync")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .build()
}
