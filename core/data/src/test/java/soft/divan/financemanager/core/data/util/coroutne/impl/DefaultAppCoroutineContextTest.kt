package soft.divan.financemanager.core.data.util.coroutne.impl

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import soft.divan.financemanager.core.data.PostCommitSyncQueue

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultAppCoroutineContextTest {

    private val handler = CoroutineExceptionHandler { _, _ -> }

    private fun context(dispatcher: kotlinx.coroutines.CoroutineDispatcher) =
        DefaultAppCoroutineContext(
            scope = CoroutineScope(dispatcher),
            dispatcher = dispatcher,
            exceptionHandler = handler
        )

    @Test
    fun `launchSync without a transaction runs the block immediately`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val appContext = context(dispatcher)
        var executed = false

        appContext.launchSync { executed = true }

        assertThat(executed).isTrue()
    }

    @Test
    fun `launchSync inside a transaction defers the block into the queue`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val appContext = context(dispatcher)
        val queue = PostCommitSyncQueue()
        var executed = false

        withContext(queue) {
            appContext.launchSync { executed = true }
        }

        assertThat(executed).isFalse()
        val deferred = queue.drain()
        assertThat(deferred).hasSize(1)

        deferred.single().invoke()
        assertThat(executed).isTrue()
    }

    @Test
    fun `drain empties the queue`() = runTest {
        val queue = PostCommitSyncQueue()
        queue.add { }
        queue.add { }

        assertThat(queue.drain()).hasSize(2)
        assertThat(queue.drain()).isEmpty()
    }
}
