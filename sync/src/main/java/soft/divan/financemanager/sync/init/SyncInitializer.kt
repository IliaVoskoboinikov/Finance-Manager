package soft.divan.financemanager.sync.init

import android.content.Context
import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import soft.divan.financemanager.sync.workers.SyncWorker

object Sync {
    fun initialize(context: Context) {
        WorkManager.getInstance(context).apply {
            enqueueUniqueWork(
                SYNC_WORK_NAME,
                ExistingWorkPolicy.KEEP,
                SyncWorker.startUpSyncWork(),
            )
            val workManager = WorkManager.getInstance(context)
            workManager.getWorkInfosForUniqueWork(SYNC_WORK_NAME).get().forEach {
                Log.d("SyncDebug", "Work info state: ${it.state}")
            }
        }
    }
}

internal const val SYNC_WORK_NAME = "SyncWorkName"
