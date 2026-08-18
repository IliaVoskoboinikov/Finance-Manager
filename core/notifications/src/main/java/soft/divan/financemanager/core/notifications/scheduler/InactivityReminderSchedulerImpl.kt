package soft.divan.financemanager.core.notifications.scheduler

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import soft.divan.financemanager.core.notifications.worker.InactivityWorker
import soft.divan.financemanager.core.workmanager.DelegatingWorker
import soft.divan.financemanager.core.workmanager.delegatedData
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InactivityReminderSchedulerImpl @Inject constructor(
    private val workManager: WorkManager
) : InactivityReminderScheduler {

    /**
     * Одноразовая задача с отложенным стартом, а не periodic.
     *
     * Семантика «напомнить через 7 дней после последнего визита» — это именно таймер,
     * который перевзводится: [ExistingWorkPolicy.REPLACE] отменяет предыдущий отсчёт и
     * начинает новый. Periodic-задача здесь не подходит — её период не сдвигается, а
     * `ExistingPeriodicWorkPolicy.REPLACE` к тому же признан устаревшим.
     *
     * Повторного взвода внутри воркера нет: за одну «паузу» пользователь получает одно
     * напоминание, следующее — только после того, как он снова откроет приложение.
     */
    override fun onUserActive() {
        val request = OneTimeWorkRequestBuilder<DelegatingWorker>()
            .setInitialDelay(INACTIVITY_THRESHOLD_DAYS, TimeUnit.DAYS)
            .setInputData(InactivityWorker::class.delegatedData())
            .build()

        workManager.enqueueUniqueWork(
            InactivityWorker.WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    override fun cancel() {
        workManager.cancelUniqueWork(InactivityWorker.WORK_NAME)
    }
}
