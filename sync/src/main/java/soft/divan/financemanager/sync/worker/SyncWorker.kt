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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import soft.divan.common.di.IoDispatcher
import soft.divan.financemanager.core.data.sync.Synchronizer
import soft.divan.financemanager.core.domain.model.LocalDataState
import soft.divan.financemanager.core.domain.usecase.ObserveLocalDataStateUseCase

@HiltWorker
internal class SyncWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncCoordinator: SyncCoordinator,
    private val observeLocalDataState: ObserveLocalDataStateUseCase,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : CoroutineWorker(appContext, workerParams), Synchronizer {

    override suspend fun getForegroundInfo(): ForegroundInfo = appContext.syncForegroundInfo()

    override suspend fun doWork(): Result = withContext(ioDispatcher) {
        traceAsync("Sync", 0) {
            if (!isLocalDataOpen()) {
                // База заперта (уровень PIN) или ключ утерян. `retry()` здесь дал бы шторм
                // повторов на закрытой базе; синк запустится сам, когда данные откроют.
                Log.d("SyncWorker", "Sync skipped: local data is not open")
                return@traceAsync Result.success()
            }
            Log.d("SyncWorker", "Sync started")
            val success = syncCoordinator.syncAll()
            if (success) {
                Log.d("SyncWorker", "Sync finished successfully")
                Result.success()
            } else {
                Result.retry()
            }
        }
    }

    /**
     * Воркер может стартовать в свежем процессе раньше, чем прочитаны ключи, — тогда ждём конца
     * инициализации, а не пропускаем синк.
     */
    private suspend fun isLocalDataOpen(): Boolean =
        observeLocalDataState().first { it !is LocalDataState.Initializing } is LocalDataState.Open
}
