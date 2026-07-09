package soft.divan.financemanager.core.data

import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

/**
 * Элемент coroutine-контекста, накапливающий отложенные sync-действия внутри БД-транзакции.
 *
 * [RoomTransactionRunner] кладёт очередь в контекст блока `runInTransaction`; репозитории
 * через `AppCoroutineContext.launchSync` добавляют сюда сетевые пуши вместо немедленного
 * запуска. После успешного commit очередь выполняется, при rollback — отбрасывается,
 * поэтому на сервер не уходят изменения, откатанные локально.
 *
 * Почему именно элемент контекста: очередь должна «доехать» от runner'а до репозиториев
 * сквозь `db.withTransaction` без передачи параметров через все слои. `ThreadLocal`
 * (как в Spring/Django post-commit hooks) с корутинами не работает — корутина мигрирует
 * между потоками; наследуемый coroutine-контекст — идиоматическая замена (тем же приёмом
 * Room передаёт собственный `TransactionElement`).
 *
 * Контракт:
 * - экземпляр одноразовый и живёт в пределах одного вызова `runInTransaction`;
 * - [add] потокобезопасен — блок транзакции может мигрировать между потоками диспетчера;
 * - порядок диспатча FIFO, но действия запускаются отдельными корутинами и между собой
 *   могут выполняться параллельно.
 *
 * Подробности и ограничения механизма: docs/post-commit-sync.md.
 */
class PostCommitSyncQueue : AbstractCoroutineContextElement(Key) {

    companion object Key : CoroutineContext.Key<PostCommitSyncQueue>

    private val actions = ConcurrentLinkedQueue<suspend () -> Unit>()

    /** Добавляет действие, которое выполнится только после успешного commit транзакции. */
    fun add(action: suspend () -> Unit) {
        actions.add(action)
    }

    /**
     * Забирает все накопленные действия в порядке добавления, очищая очередь.
     * Вызывается [RoomTransactionRunner] строго после успешного commit;
     * при rollback не вызывается — действия отбрасываются вместе с очередью.
     */
    fun drain(): List<suspend () -> Unit> {
        val drained = mutableListOf<suspend () -> Unit>()
        while (true) {
            val action = actions.poll() ?: break
            drained.add(action)
        }
        return drained
    }
}
