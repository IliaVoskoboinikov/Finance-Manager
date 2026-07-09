package soft.divan.financemanager.core.data.util.coroutne

import kotlinx.coroutines.CoroutineScope

/**
 * Запуск фоновой работы data-слоя на application-scope (переживает экраны и ViewModel'и).
 *
 * Инкапсулирует scope/dispatcher/exceptionHandler, чтобы репозитории не зависели от
 * конкретных диспетчеров и не создавали ad-hoc скоупов.
 */
interface AppCoroutineContext {

    /**
     * Немедленно запускает [block] на application-scope (IO + exceptionHandler).
     *
     * Не учитывает границу БД-транзакции — для пушей после записи используйте [launchSync].
     * Подходит для pull-триггеров из не-suspend методов (`getAll()` и других Flow-методов),
     * где coroutine-контекст вызывающего недоступен.
     */
    fun launch(block: suspend CoroutineScope.() -> Unit)

    /**
     * Запускает фоновую синхронизацию, уважая границу БД-транзакции.
     *
     * Вне транзакции ведёт себя как [launch]. Внутри `runInTransaction` действие
     * откладывается до успешного commit и отбрасывается при rollback — иначе
     * fire-and-forget пуш успел бы отправить на сервер изменения, откатанные локально.
     *
     * Используйте в suspend-методах репозиториев для любых сетевых эффектов после записи.
     * Механизм: PostCommitSyncQueue в coroutine-контексте (docs/post-commit-sync.md).
     */
    suspend fun launchSync(block: suspend () -> Unit)
}
