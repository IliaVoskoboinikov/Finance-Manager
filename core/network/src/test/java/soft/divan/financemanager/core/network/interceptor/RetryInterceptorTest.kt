package soft.divan.financemanager.core.network.interceptor

import android.os.SystemClock
import android.util.Log
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import okhttp3.Interceptor
import okhttp3.Response
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test

class RetryInterceptorTest {

    private val request = HttpTestFactory.request()

    @Before
    fun setUp() {
        // RetryInterceptor touches Android framework (Log + SystemClock) which is stubbed in
        // plain JVM unit tests; replace them with no-ops.
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        mockkStatic(SystemClock::class)
        every { SystemClock.sleep(any()) } returns Unit
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    private fun chainReturning(vararg responses: Response): Interceptor.Chain {
        val chain = mockk<Interceptor.Chain>()
        every { chain.request() } returns request
        every { chain.proceed(any()) } returnsMany responses.toList()
        return chain
    }

    @Test
    fun `returns immediately on success without retrying`() {
        val chain = chainReturning(HttpTestFactory.response(request, 200))
        val interceptor = RetryInterceptor(maxRetries = 3, delayMillis = 0)

        val result = interceptor.intercept(chain)

        assertThat(result.code).isEqualTo(200)
        verify(exactly = 1) { chain.proceed(any()) }
    }

    @Test
    fun `retries on server error then returns success`() {
        val chain = chainReturning(
            HttpTestFactory.response(request, 503),
            HttpTestFactory.response(request, 200)
        )
        val interceptor = RetryInterceptor(maxRetries = 3, delayMillis = 0)

        val result = interceptor.intercept(chain)

        assertThat(result.code).isEqualTo(200)
        verify(exactly = 2) { chain.proceed(any()) }
    }

    @Test
    fun `stops after maxRetries and returns last server error`() {
        val chain = chainReturning(
            HttpTestFactory.response(request, 500),
            HttpTestFactory.response(request, 500),
            HttpTestFactory.response(request, 500)
        )
        val interceptor = RetryInterceptor(maxRetries = 2, delayMillis = 0)

        val result = interceptor.intercept(chain)

        assertThat(result.code).isEqualTo(500)
        // initial attempt + 2 retries
        verify(exactly = 3) { chain.proceed(any()) }
    }

    @Test
    fun `does not retry on client error`() {
        val chain = chainReturning(HttpTestFactory.response(request, 404))
        val interceptor = RetryInterceptor(maxRetries = 3, delayMillis = 0)

        val result = interceptor.intercept(chain)

        assertThat(result.code).isEqualTo(404)
        verify(exactly = 1) { chain.proceed(any()) }
    }

    @Test
    fun `does not retry when maxRetries is zero`() {
        val chain = chainReturning(
            HttpTestFactory.response(request, 500),
            HttpTestFactory.response(request, 200)
        )
        val interceptor = RetryInterceptor(maxRetries = 0, delayMillis = 0)

        val result = interceptor.intercept(chain)

        assertThat(result.code).isEqualTo(500)
        verify(exactly = 1) { chain.proceed(any()) }
    }

    @Test
    fun `sleeps between retries`() {
        val chain = chainReturning(
            HttpTestFactory.response(request, 502),
            HttpTestFactory.response(request, 200)
        )
        val interceptor = RetryInterceptor(maxRetries = 3, delayMillis = 1_000)

        interceptor.intercept(chain)

        verify(exactly = 1) { SystemClock.sleep(1_000) }
    }
}
