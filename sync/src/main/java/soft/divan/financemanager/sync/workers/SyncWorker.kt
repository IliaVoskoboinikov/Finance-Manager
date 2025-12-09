package soft.divan.financemanager.sync.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.tracing.traceAsync
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import soft.divan.financemanager.core.data.Synchronizer
import soft.divan.financemanager.core.data.repository.AccountRepositoryImpl
import soft.divan.financemanager.core.data.repository.CategoryRepositoryImpl
import soft.divan.financemanager.sync.init.SyncConstraints
import soft.divan.financemanager.sync.init.syncForegroundInfo


@HiltWorker
internal class SyncWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val categoryRepositoryImpl: CategoryRepositoryImpl,
    private val accountRepositoryImpl: AccountRepositoryImpl,
    private val ioDispatcher: CoroutineDispatcher,
) : CoroutineWorker(appContext, workerParams), Synchronizer {


    override suspend fun getForegroundInfo(): ForegroundInfo =
        appContext.syncForegroundInfo()

    override suspend fun doWork(): Result = withContext(ioDispatcher) {
        traceAsync("Sync", 0) {
            Log.d("SyncWorker", "doWork: ")

            val syncedSuccessfully = awaitAll(
                async { categoryRepositoryImpl.sync() },
                async { accountRepositoryImpl.sync() },
            ).all { it }

            if (syncedSuccessfully) {
                markLastSyncedTime()
                Result.success()
            } else {
                Result.retry()
            }
        }
    }

    fun markLastSyncedTime() {
        //  TODO("Not yet implemented")
    }

    companion object {

        fun startUpSyncWork() = OneTimeWorkRequestBuilder<DelegatingWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setConstraints(SyncConstraints)
            .setInputData(SyncWorker::class.delegatedData())
            .build()

    }


}
