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
     * Шаги выполняются **все**, независимо от исхода предыдущих. Сбой чтения категорий — не повод
     * оставить счета и транзакции без обновления: каждый шаг самодостаточен и приносит пользу сам
     * по себе, а недостающее подтянет ближайший прогон.
     *
     * Очередь обрабатывается **последней и тоже всегда**: накопленные локальные изменения не должны
     * ждать отправки из-за проблем с чтением — оно на неё не влияет.
     *
     * Возвращает `true`, только если удались все шаги: по этому признаку [SyncCoordinator]
     * решает, обновлять ли время последней успешной синхронизации и просить ли WorkManager
     * повторить работу.
     */
    override suspend fun syncAll(): Boolean {
        // Порядок важен — категории нужны транзакциям, счета тоже, — но неудача шага не отменяет
        // следующие: они работают с тем, что уже есть локально, и приносят частичный прогресс.
        // Результаты собираем в переменные, иначе `&&` пропустил бы оставшиеся шаги.
        val categoriesPulled = runStep("CategorySync") { categorySyncManager.sync() }
        val accountsPulled = runStep("AccountSync") { accountSyncManager.sync() }
        val transactionsPulled = runStep("TransactionSync") { transactionSyncManager.sync() }

        val pulled = categoriesPulled && accountsPulled && transactionsPulled

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
