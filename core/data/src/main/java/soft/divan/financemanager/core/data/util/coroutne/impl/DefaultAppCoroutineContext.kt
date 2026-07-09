package soft.divan.financemanager.core.data.util.coroutne.impl

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.launch
import soft.divan.common.di.ApplicationScope
import soft.divan.common.di.IoDispatcher
import soft.divan.financemanager.core.data.PostCommitSyncQueue
import soft.divan.financemanager.core.data.util.coroutne.AppCoroutineContext

/**
 * Реализация [AppCoroutineContext]: application-scope + IO-диспетчер + exceptionHandler
 * (упавшая фоновая работа логируется и не роняет процесс — scope с SupervisorJob).
 *
 * [launchSync] маршрутизирует по наличию [PostCommitSyncQueue] в текущем coroutine-контексте:
 * очередь кладёт туда `RoomTransactionRunner` на время БД-транзакции.
 */
class DefaultAppCoroutineContext(
    @param:ApplicationScope val scope: CoroutineScope,
    @param:IoDispatcher val dispatcher: CoroutineDispatcher,
    val exceptionHandler: CoroutineExceptionHandler
) : AppCoroutineContext {

    override fun launch(block: suspend CoroutineScope.() -> Unit) {
        scope.launch(dispatcher + exceptionHandler) {
            block()
        }
    }

    override suspend fun launchSync(block: suspend () -> Unit) {
        val queue = currentCoroutineContext()[PostCommitSyncQueue]
        if (queue != null) {
            // Внутри БД-транзакции: откладываем до успешного commit (см. RoomTransactionRunner).
            queue.add(block)
        } else {
            launch { block() }
        }
    }
}
