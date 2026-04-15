package soft.divan.financemanager.core.notifications.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import soft.divan.financemanager.core.notifications.NotificationHelper
import soft.divan.financemanager.core.notifications.R
import soft.divan.financemanager.core.notifications.model.NotificationMessage

@HiltWorker
class InactivityWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val title = context.getString(R.string.notification_inactivity_title)
        val message = context.getString(R.string.notification_inactivity_message)

        notificationHelper.showNotification(
            NotificationMessage(
                title = title,
                body = message
            )
        )

        return Result.success()
    }

    companion object {
        const val WORK_NAME = "InactivityNotificationWork"
    }
}
