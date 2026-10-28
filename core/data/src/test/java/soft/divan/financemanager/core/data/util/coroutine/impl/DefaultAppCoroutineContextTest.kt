package soft.divan.financemanager.core.data.util.coroutine.impl

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import soft.divan.financemanager.core.data.transaction.PostCommitSyncQueue

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultAppCoroutineContextTest {

    private val handler = CoroutineExceptionHandler { _, _ -> }

    private fun context(dispatcher: CoroutineDispatcher) =
        DefaultAppCoroutineContext(
            scope = CoroutineScope(dispatcher),
            dispatcher = dispatcher,
            exceptionHandler = handler
        )

    @Test
    fun `launch executes block on injected dispatcher`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val scope = CoroutineScope(SupervisorJob())
        val context = DefaultAppCoroutineContext(scope, dispatcher, handler)
        var executed = false

        context.launch { executed = true }

        assertThat(executed).isFalse()
        advanceUntilIdle()
        assertThat(executed).isTrue()
    }

    @Test
    fun `launch routes uncaught exceptions to the injected handler`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val scope = CoroutineScope(SupervisorJob())
        var captured: Throwable? = null
        val capturingHandler = CoroutineExceptionHandler { _, throwable -> captured = throwable }
        val context = DefaultAppCoroutineContext(scope, dispatcher, capturingHandler)
        val boom = IllegalStateException("boom")

        context.launch { throw boom }
        advanceUntilIdle()

        assertThat(captured).isSameAs(boom)
    }

    @Test
    fun `failed block does not cancel subsequent launches`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val scope = CoroutineScope(SupervisorJob())
        val context = DefaultAppCoroutineContext(scope, dispatcher, handler)
        var secondRan = false

        context.launch { throw IllegalStateException("first fails") }
        advanceUntilIdle()
        context.launch { secondRan = true }
        advanceUntilIdle()

        assertThat(secondRan).isTrue()
    }

    @Test
    fun `exposes injected scope dispatcher and handler`() {
        val dispatcher = StandardTestDispatcher()
        val scope = CoroutineScope(SupervisorJob())

        val context = DefaultAppCoroutineContext(scope, dispatcher, handler)

        assertThat(context.scope).isSameAs(scope)
        assertThat(context.dispatcher).isSameAs(dispatcher)
        assertThat(context.exceptionHandler).isSameAs(handler)
    }

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
