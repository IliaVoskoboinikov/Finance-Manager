package soft.divan.financemanager.sync.worker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.ForegroundInfo
import soft.divan.financemanager.sync.R

private const val SYNC_NOTIFICATION_ID = 1
private const val SYNC_NOTIFICATION_CHANNEL_ID = "SyncNotificationChannel"

/**
 * Foreground information for sync on lower API levels when sync workers are being
 * run with a foreground service
 */
fun Context.syncForegroundInfo() = ForegroundInfo(
    SYNC_NOTIFICATION_ID,
    syncWorkNotification()
)

/**
 * Notification displayed on lower API levels when sync workers are being
 * run with a foreground service
 */
private fun Context.syncWorkNotification(): Notification {
    val channel = NotificationChannel(
        SYNC_NOTIFICATION_CHANNEL_ID,
        "sync",
        NotificationManager.IMPORTANCE_DEFAULT
    ).apply {
        description = "Sync"
    }
    // Register the channel with the system
    val notificationManager: NotificationManager? =
        getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

    notificationManager?.createNotificationChannel(channel)

    return NotificationCompat.Builder(
        this,
        SYNC_NOTIFICATION_CHANNEL_ID
    )
        .setSmallIcon(R.drawable.ic_sync_notification)
        .setContentTitle("Sync")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .build()
}
