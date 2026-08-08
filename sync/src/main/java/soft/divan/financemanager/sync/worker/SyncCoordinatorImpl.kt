package soft.divan.financemanager.sync.worker

import android.util.Log
import soft.divan.financemanager.core.data.outbox.OutboxProcessor
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
    private val outboxProcessor: OutboxProcessor,
    private val setLastSyncTimeUseCase: SetLastSyncTimeUseCase
) : SyncCoordinator, Synchronizer {

    /**
     * Полный цикл синхронизации: сначала забираем изменения с сервера, затем разбираем очередь
     * исходящих операций.
     *
     * Очередь обрабатывается **последней и всегда**, даже если какой-то pull не удался: накопленные
     * локальные изменения не должны ждать из-за проблем с чтением — их отправка не зависит от него.
     */
    override suspend fun syncAll(): Boolean {
        val pulled =
            runStep("CategorySync") { categorySyncManager.sync() } &&
                runStep("AccountSync") { accountSyncManager.sync() } &&
                runStep("TransactionSync") { transactionSyncManager.sync() }

        val pushed = runStep("OutboxSync") {
            outboxProcessor.process()
            true
        }

        val success = pulled && pushed
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
