package soft.divan.financemanager.core.notifications.worker.sheduler

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import soft.divan.financemanager.core.notifications.worker.InactivityWorker
import soft.divan.financemanager.core.workmanager.DelegatingWorker
import soft.divan.financemanager.core.workmanager.delegatedData
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationSchedulerImpl @Inject constructor(
    private val workManager: WorkManager
) : NotificationScheduler {

    override fun scheduleInactivityNotification() {
        val workRequest = PeriodicWorkRequestBuilder<DelegatingWorker>(
            INACTIVITY_INTERVAL_DAYS,
            TimeUnit.DAYS
        )
            .setInitialDelay(INACTIVITY_INTERVAL_DAYS, TimeUnit.DAYS)
            .setInputData(InactivityWorker::class.delegatedData())
            .build()

        workManager.enqueueUniquePeriodicWork(
            InactivityWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }

    companion object {
        private const val INACTIVITY_INTERVAL_DAYS = 7L
    }
}
