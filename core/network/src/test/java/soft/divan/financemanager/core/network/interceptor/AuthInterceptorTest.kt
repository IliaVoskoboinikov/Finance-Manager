package soft.divan.financemanager.core.network.interceptor

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import okhttp3.Interceptor
import okhttp3.Request
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class AuthInterceptorTest {

    private val originalRequest = HttpTestFactory.request()

    private fun chainReturning(captured: io.mockk.CapturingSlot<Request>): Interceptor.Chain {
        val chain = mockk<Interceptor.Chain>()
        every { chain.request() } returns originalRequest
        every { chain.proceed(capture(captured)) } answers {
            HttpTestFactory.response(captured.captured, 200)
        }
        return chain
    }

    @Test
    fun `adds Authorization header with bearer token`() {
        val slot = slot<Request>()
        val interceptor = AuthInterceptor { "my-token" }

        interceptor.intercept(chainReturning(slot))

        assertThat(slot.captured.header("Authorization")).isEqualTo("Bearer my-token")
    }

    @Test
    fun `keeps original url and method`() {
        val slot = slot<Request>()
        val interceptor = AuthInterceptor { "t" }

        interceptor.intercept(chainReturning(slot))

        assertThat(slot.captured.url).isEqualTo(originalRequest.url)
        assertThat(slot.captured.method).isEqualTo("GET")
    }

    @Test
    fun `evaluates token lazily on each request`() {
        val slot = slot<Request>()
        var current = "first"
        val interceptor = AuthInterceptor { current }

        interceptor.intercept(chainReturning(slot))
        assertThat(slot.captured.header("Authorization")).isEqualTo("Bearer first")

        current = "second"
        val slot2 = slot<Request>()
        interceptor.intercept(chainReturning(slot2))
        assertThat(slot2.captured.header("Authorization")).isEqualTo("Bearer second")
    }

    @Test
    fun `proceeds with the modified request exactly once`() {
        val slot = slot<Request>()
        val chain = chainReturning(slot)
        val interceptor = AuthInterceptor { "t" }

        interceptor.intercept(chain)

        verify(exactly = 1) { chain.proceed(any()) }
    }
}
