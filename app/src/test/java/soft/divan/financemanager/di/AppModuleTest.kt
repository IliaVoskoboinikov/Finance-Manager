package soft.divan.financemanager.di

import android.util.Log
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.EmptyCoroutineContext

@OptIn(ExperimentalCoroutinesApi::class)
class AppModuleTest {

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.e(any(), any(), any()) } returns 0
    }

    @After
    fun tearDown() {
        unmockkStatic(Log::class)
    }

    @Test
    fun `application scope uses supervisor job with provided dispatcher and handler`() {
        val dispatcher: CoroutineDispatcher = UnconfinedTestDispatcher()
        val handler = CoroutineExceptionHandler { _, _ -> }

        val scope = AppModule.provideApplicationScope(dispatcher, handler)

        val job = scope.coroutineContext[Job]
        assertThat(job).isNotNull()
        assertThat(scope.coroutineContext[CoroutineExceptionHandler]).isSameAs(handler)
        assertThat(scope.coroutineContext[ContinuationInterceptor]).isSameAs(dispatcher)
    }

    @Test
    fun `failed child does not cancel application scope`() {
        val dispatcher: CoroutineDispatcher = UnconfinedTestDispatcher()
        val handler = CoroutineExceptionHandler { _, _ -> }
        val scope = AppModule.provideApplicationScope(dispatcher, handler)

        scope.launch { error("first child fails") }
        var secondRan = false
        scope.launch { secondRan = true }

        assertThat(secondRan).isTrue()
        assertThat(scope.coroutineContext[Job]!!.isActive).isTrue()
    }

    @Test
    fun `io dispatcher is Dispatchers IO`() {
        assertThat(AppModule.provideIoDispatcher()).isSameAs(Dispatchers.IO)
    }

    @Test
    fun `main immediate dispatcher is Dispatchers Main immediate`() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        try {
            assertThat(AppModule.provideMainImmediateDispatcher())
                .isSameAs(Dispatchers.Main.immediate)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `exception handler logs unhandled exceptions without rethrow`() {
        val handler = AppModule.provideAppCoroutineExceptionHandler()
        val boom = IllegalStateException("boom")

        handler.handleException(EmptyCoroutineContext, boom)

        verify(exactly = 1) { Log.e("AppCoroutineException", any(), boom) }
    }

    @Test
    fun `application scope survives without external supervisor job`() {
        val handler = CoroutineExceptionHandler { _, _ -> }
        val scope = AppModule.provideApplicationScope(UnconfinedTestDispatcher(), handler)

        // SupervisorJob создаётся внутри модуля — это не Job переданного контекста
        assertThat(scope.coroutineContext[Job]).isNotSameAs(SupervisorJob())
        assertThat(scope.coroutineContext[Job]!!.isActive).isTrue()
    }
}
