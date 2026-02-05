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
import soft.divan.financemanager.sync.initializers.SYNC_ONE_TIME_WORK
import soft.divan.financemanager.sync.initializers.SYNC_PERIODIC_WORK
import soft.divan.financemanager.sync.worker.DelegatingWorker
import soft.divan.financemanager.sync.worker.SyncConstraints
import soft.divan.financemanager.sync.worker.SyncWorker
import soft.divan.financemanager.sync.worker.delegatedData
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

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                SYNC_PERIODIC_WORK,
                ExistingPeriodicWorkPolicy.KEEP,
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
