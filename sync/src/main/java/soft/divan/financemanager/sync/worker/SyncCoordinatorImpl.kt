package soft.divan.financemanager.sync.worker

import android.util.Log
import soft.divan.financemanager.core.data.sync.AccountSyncManager
import soft.divan.financemanager.core.data.sync.CategorySyncManager
import soft.divan.financemanager.core.data.sync.TransactionSyncManager
import soft.divan.financemanager.core.data.sync.util.Synchronizer
import soft.divan.financemanager.sync.domain.usecase.SetLastSyncTimeUseCase
import javax.inject.Inject

class SyncCoordinatorImpl @Inject constructor(
    private val categorySyncManager: CategorySyncManager,
    private val accountSyncManager: AccountSyncManager,
    private val transactionSyncManager: TransactionSyncManager,
    private val setLastSyncTimeUseCase: SetLastSyncTimeUseCase
) : SyncCoordinator, Synchronizer {

    override suspend fun syncAll(): Boolean {
        val success =
            runStep("CategorySync") { categorySyncManager.sync() } &&
                runStep("AccountSync") { accountSyncManager.sync() } &&
                runStep("TransactionSync") { transactionSyncManager.sync() }

        if (success) {
            setLastSyncTimeUseCase(System.currentTimeMillis())
        }

        return success
    }

    private suspend inline fun runStep(
        name: String,
        crossinline block: suspend () -> Boolean
    ): Boolean {
        Log.d("SyncCoordinator", "Start $name")
        val result = runCatching { block() }.getOrDefault(false)
        Log.d("SyncCoordinator", "Finish $name: $result")
        return result
    }
}
