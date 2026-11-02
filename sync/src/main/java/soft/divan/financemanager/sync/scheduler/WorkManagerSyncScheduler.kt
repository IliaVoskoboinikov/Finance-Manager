package soft.divan.financemanager.sync.scheduler

import android.content.Context
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import soft.divan.financemanager.core.workmanager.DelegatingWorker
import soft.divan.financemanager.core.workmanager.delegatedData
import soft.divan.financemanager.sync.initializers.SYNC_ONE_TIME_WORK
import soft.divan.financemanager.sync.initializers.SYNC_PERIODIC_WORK
import soft.divan.financemanager.sync.worker.SyncConstraints
import soft.divan.financemanager.sync.worker.SyncWorker
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class WorkManagerSyncScheduler @Inject constructor(
    @param:ApplicationContext private val context: Context
) : SyncScheduler {

    override fun schedulePeriodicSync(hours: Int) {
        Log.d("schedulePeriodicSync", "hours: $hours")

        val request = PeriodicWorkRequestBuilder<DelegatingWorker>(
            hours.toLong(),
            TimeUnit.HOURS
        )
            .setConstraints(SyncConstraints)
            .setInputData(SyncWorker::class.delegatedData())
            .build()

        // UPDATE, а не KEEP: KEEP оставил бы задачу со старым периодом, и смена интервала
        // в настройках ни на что не влияла бы. UPDATE меняет спецификацию на месте и сохраняет
        // время прошлого запуска, поэтому вызов на каждом старте приложения не сдвигает отсчёт,
        // а уже идущий синк не прерывается — новый период применяется со следующего запуска.
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                SYNC_PERIODIC_WORK,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
    }

    override fun scheduleOneTimeSync() {
        val request = OneTimeWorkRequestBuilder<DelegatingWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setConstraints(SyncConstraints)
            .setInputData(SyncWorker::class.delegatedData())
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                SYNC_ONE_TIME_WORK,
                ExistingWorkPolicy.REPLACE,
                request
            )
    }
}
