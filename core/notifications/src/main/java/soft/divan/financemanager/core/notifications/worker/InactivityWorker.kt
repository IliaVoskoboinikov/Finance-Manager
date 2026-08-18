package soft.divan.financemanager.core.notifications.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import soft.divan.financemanager.core.notifications.NotificationHelper
import soft.divan.financemanager.core.notifications.R
import soft.divan.financemanager.core.notifications.model.NotificationIds
import soft.divan.financemanager.core.notifications.model.NotificationMessage

/**
 * Показывает напоминание о неактивности.
 *
 * Ставится в очередь не напрямую, а через `DelegatingWorker` (см. `:core:workmanager`):
 * приложение не переопределяет конфигурацию WorkManager, поэтому системная фабрика не
 * умеет собирать `@HiltWorker` с зависимостями.
 */
@HiltWorker
class InactivityWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        notificationHelper.showNotification(
            NotificationMessage(
                id = NotificationIds.INACTIVITY_REMINDER,
                title = context.getString(R.string.notification_inactivity_title),
                body = context.getString(R.string.notification_inactivity_message)
            )
        )

        return Result.success()
    }

    companion object {
        const val WORK_NAME = "InactivityNotificationWork"
    }
}
