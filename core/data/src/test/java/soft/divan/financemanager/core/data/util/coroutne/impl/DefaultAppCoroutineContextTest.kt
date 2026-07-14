package soft.divan.financemanager.core.data.util.coroutne.impl

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultAppCoroutineContextTest {

    @Test
    fun `launch executes block on injected dispatcher`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val scope = CoroutineScope(SupervisorJob())
        val handler = CoroutineExceptionHandler { _, _ -> }
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
        val handler = CoroutineExceptionHandler { _, throwable -> captured = throwable }
        val context = DefaultAppCoroutineContext(scope, dispatcher, handler)
        val boom = IllegalStateException("boom")

        context.launch { throw boom }
        advanceUntilIdle()

        assertThat(captured).isSameAs(boom)
    }

    @Test
    fun `failed block does not cancel subsequent launches`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val scope = CoroutineScope(SupervisorJob())
        val handler = CoroutineExceptionHandler { _, _ -> }
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
        val handler = CoroutineExceptionHandler { _, _ -> }

        val context = DefaultAppCoroutineContext(scope, dispatcher, handler)

        assertThat(context.scope).isSameAs(scope)
        assertThat(context.dispatcher).isSameAs(dispatcher)
        assertThat(context.exceptionHandler).isSameAs(handler)
    }
}
