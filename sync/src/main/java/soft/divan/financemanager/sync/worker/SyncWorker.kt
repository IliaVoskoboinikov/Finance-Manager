package soft.divan.financemanager.sync.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.tracing.traceAsync
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import soft.divan.common.di.IoDispatcher
import soft.divan.financemanager.core.data.sync.impl.AccountSyncManagerImpl
import soft.divan.financemanager.core.data.sync.impl.CategorySyncManagerImpl
import soft.divan.financemanager.core.data.sync.impl.TransactionSyncManagerImpl
import soft.divan.financemanager.core.data.sync.util.Synchronizer
import soft.divan.financemanager.sync.domain.usecase.SetLastSyncTimeUseCase

@HiltWorker
internal class SyncWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val categorySyncManagerImpl: CategorySyncManagerImpl,
    private val accountSyncManagerImpl: AccountSyncManagerImpl,
    private val transactionSyncManagerImpl: TransactionSyncManagerImpl,
    private val setLastSyncTimeUseCase: SetLastSyncTimeUseCase,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : CoroutineWorker(appContext, workerParams), Synchronizer {

    override suspend fun getForegroundInfo(): ForegroundInfo = appContext.syncForegroundInfo()

    override suspend fun doWork(): Result = withContext(ioDispatcher) {
        traceAsync("Sync", 0) {
            Log.d("SyncWorker", "doWork: ")

            val syncedSuccessfully = awaitAll(
                async { categorySyncManagerImpl.sync() },
                async { accountSyncManagerImpl.sync() },
                async { transactionSyncManagerImpl.sync() },
            ).all { it }

            if (syncedSuccessfully) {
                setLastSyncTimeUseCase(System.currentTimeMillis())
                Result.success()
            } else {
                Result.retry()
            }
        }
    }
}
