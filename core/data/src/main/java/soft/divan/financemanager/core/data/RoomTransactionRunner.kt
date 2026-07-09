package soft.divan.financemanager.core.data

import androidx.room.withTransaction
import kotlinx.coroutines.withContext
import soft.divan.financemanager.core.data.util.coroutne.AppCoroutineContext
import soft.divan.financemanager.core.database.db.FinanceManagerDatabase
import soft.divan.financemanager.core.domain.result.DomainResult
import javax.inject.Inject

/**
 * Реализация [TransactionRunner] поверх Room `withTransaction` с поддержкой отложенных
 * сетевых пушей (post-commit sync, см. docs/post-commit-sync.md).
 *
 * Порядок работы:
 * 1. На каждый вызов создаётся своя [PostCommitSyncQueue] и кладётся в coroutine-контекст
 *    блока — элементы контекста наследуются сквозь `db.withTransaction`, поэтому
 *    репозитории внутри блока видят очередь через `launchSync` без передачи параметров.
 * 2. Блок выполняется в БД-транзакции; `rollbackOnError()` откатывает её исключением.
 * 3. Commit → накопленные пуши запускаются на [appCoroutineContext] (application-scope,
 *    IO + exceptionHandler). Rollback → до диспатча не доходим, пуши отбрасываются:
 *    на сервер не уходят изменения, откатанные локально.
 *
 * Гарантию доставки при крэше между commit и диспатчем обеспечивает не эта очередь,
 * а `syncStatus = PENDING_*` + фоновый синк: немедленный пуш — только оптимизация.
 */
class RoomTransactionRunner @Inject constructor(
    private val db: FinanceManagerDatabase,
    private val appCoroutineContext: AppCoroutineContext
) : TransactionRunner {

    override suspend fun <T> runInTransaction(block: suspend () -> T): T {
        // Очередь отложенных пушей: репозитории внутри блока кладут сюда сетевые sync-действия
        // (через AppCoroutineContext.launchSync) вместо немедленного запуска.
        val postCommitQueue = PostCommitSyncQueue()
        return try {
            val result = withContext(postCommitQueue) {
                db.withTransaction {
                    block()
                }
            }
            // Commit прошёл — запускаем отложенные синки. При rollback сюда не попадаем,
            // и накопленные действия отбрасываются вместе с очередью.
            postCommitQueue.drain().forEach { action ->
                appCoroutineContext.launch { action() }
            }
            result
        } catch (e: TransactionRollbackException) {
            // Если мы сами выбросили это исключение, возвращаем Failure
            @Suppress("UNCHECKED_CAST")
            DomainResult.Failure(e.error) as T
        }
    }
}
