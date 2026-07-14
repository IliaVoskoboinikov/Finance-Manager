package soft.divan.financemanager.core.data.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import soft.divan.financemanager.core.data.util.coroutne.AppCoroutineContext

/**
 * Тестовый [AppCoroutineContext]: не запускает блоки сразу, а записывает их,
 * чтобы тест мог сначала проверить синхронный результат метода репозитория,
 * а затем детерминированно выполнить фоновые блоки через [runAll].
 */
class RecordingAppCoroutineContext : AppCoroutineContext {

    private val blocks = mutableListOf<suspend CoroutineScope.() -> Unit>()

    val launchCount: Int get() = blocks.size

    override fun launch(block: suspend CoroutineScope.() -> Unit) {
        blocks += block
    }

    /** Выполняет все записанные фоновые блоки в порядке постановки. */
    suspend fun runAll() {
        coroutineScope {
            blocks.toList().forEach { block -> block() }
        }
        blocks.clear()
    }
}
