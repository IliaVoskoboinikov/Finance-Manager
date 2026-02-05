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
    private val categorySyncManager: CategorySyncManagerImpl,
    private val accountSyncManager: AccountSyncManagerImpl,
    private val transactionSyncManager: TransactionSyncManagerImpl,
    private val setLastSyncTimeUseCase: SetLastSyncTimeUseCase,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : CoroutineWorker(appContext, workerParams), Synchronizer {

    override suspend fun getForegroundInfo(): ForegroundInfo = appContext.syncForegroundInfo()

    override suspend fun doWork(): Result = withContext(ioDispatcher) {
        traceAsync("Sync", 0) {
            Log.d("SyncWorker", "doWork: ")
            Log.d("SyncWorker", "Sync started")

            // 1. Категории (не зависят ни от кого)
            if (!runStep("CategorySync") { categorySyncManager.sync() }) {
                return@traceAsync Result.retry()
            }

            // 2. Аккаунты (основа для транзакций)
            if (!runStep("AccountSync") { accountSyncManager.sync() }) {
                return@traceAsync Result.retry()
            }

            // 3. Транзакции (СТРОГО после аккаунтов)
            if (!runStep("TransactionSync") { transactionSyncManager.sync() }) {
                return@traceAsync Result.retry()
            }

            setLastSyncTimeUseCase(System.currentTimeMillis())
            Log.d("SyncWorker", "Sync finished successfully")

            Result.success()
        }
    }

    private suspend inline fun runStep(
        name: String,
        crossinline block: suspend () -> Boolean
    ): Boolean {
        Log.d("SyncWorker", "Start $name")
        val result = runCatching { block() }.getOrDefault(false)
        Log.d("SyncWorker", "Finish $name: $result")
        return result
    }
}
