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
        val interceptor = RetryInterceptor(maxRetries = 3, baseDelayMillis = 0)

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
        val interceptor = RetryInterceptor(maxRetries = 3, baseDelayMillis = 0)

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
        val interceptor = RetryInterceptor(maxRetries = 2, baseDelayMillis = 0)

        val result = interceptor.intercept(chain)

        assertThat(result.code).isEqualTo(500)
        // initial attempt + 2 retries
        verify(exactly = 3) { chain.proceed(any()) }
    }

    @Test
    fun `does not retry on client error`() {
        val chain = chainReturning(HttpTestFactory.response(request, 404))
        val interceptor = RetryInterceptor(maxRetries = 3, baseDelayMillis = 0)

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
        val interceptor = RetryInterceptor(maxRetries = 0, baseDelayMillis = 0)

        val result = interceptor.intercept(chain)

        assertThat(result.code).isEqualTo(500)
        verify(exactly = 1) { chain.proceed(any()) }
    }

    @Test
    fun `sleeps between retries within the backoff bounds`() {
        val chain = chainReturning(
            HttpTestFactory.response(request, 502),
            HttpTestFactory.response(request, 200)
        )
        val interceptor = RetryInterceptor(maxRetries = 3, baseDelayMillis = 1_000)

        interceptor.intercept(chain)

        // first retry: capped = 1000, equal jitter -> sleep in [500, 1000]
        verify(exactly = 1) { SystemClock.sleep(match { it in 500L..1_000L }) }
    }

    @Test
    fun `backoff grows exponentially within equal-jitter bounds`() {
        val interceptor = RetryInterceptor(baseDelayMillis = 100, maxDelayMillis = 10_000)

        assertThat(interceptor.backoffDelayMillis(1)).isBetween(50L, 100L)
        assertThat(interceptor.backoffDelayMillis(2)).isBetween(100L, 200L)
        assertThat(interceptor.backoffDelayMillis(3)).isBetween(200L, 400L)
        assertThat(interceptor.backoffDelayMillis(4)).isBetween(400L, 800L)
    }

    @Test
    fun `backoff is capped at maxDelay`() {
        val interceptor = RetryInterceptor(baseDelayMillis = 1_000, maxDelayMillis = 1_500)

        repeat(20) {
            assertThat(interceptor.backoffDelayMillis(10)).isBetween(750L, 1_500L)
        }
    }

    @Test
    fun `backoff is zero when base delay is zero`() {
        val interceptor = RetryInterceptor(baseDelayMillis = 0)

        assertThat(interceptor.backoffDelayMillis(1)).isZero()
        assertThat(interceptor.backoffDelayMillis(5)).isZero()
    }
}
