package soft.divan.financemanager.core.auth.data.interceptor

import android.util.Log
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Before
import org.junit.Test
import soft.divan.financemanager.core.auth.domain.model.AuthStatus
import soft.divan.financemanager.core.auth.domain.provider.AuthStateProvider
import javax.inject.Provider

class GuestAccessInterceptorTest {

    private val authStateProvider: AuthStateProvider = mockk()
    private val authStateProviderWrapper: Provider<AuthStateProvider> = Provider { authStateProvider }
    private lateinit var interceptor: GuestAccessInterceptor
    private val chain: Interceptor.Chain = mockk()

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.e(any(), any<String>()) } returns 0
        interceptor = GuestAccessInterceptor(authStateProviderWrapper)
    }

    @org.junit.After
    fun tearDown() {
        unmockkStatic(Log::class)
    }

    @Test
    fun `intercept should proceed when authorized`() {
        // Given
        val request = Request.Builder().url("https://api.example.com").build()
        val response = mockk<Response>()
        every { chain.request() } returns request
        every { authStateProvider.currentStatus() } returns AuthStatus.AUTHORIZED
        every { chain.proceed(request) } returns response

        // When
        interceptor.intercept(chain)

        // Then
        io.mockk.verify { chain.proceed(request) }
    }

    @Test
    fun `intercept should proceed when allow guest header is present`() {
        // Given
        val request = Request.Builder()
            .url("https://api.example.com")
            .header("X-Allow-Guest", "true")
            .build()
        val response = mockk<Response>()
        every { chain.request() } returns request
        every { chain.proceed(request) } returns response

        // When
        interceptor.intercept(chain)

        // Then
        io.mockk.verify { chain.proceed(request) }
        io.mockk.verify(exactly = 0) { authStateProvider.currentStatus() }
    }

    @Test
    fun `intercept should throw GuestModeNetworkBlockedException when status is GUEST`() {
        // Given
        val request = Request.Builder().url("https://api.example.com").build()
        every { chain.request() } returns request
        every { authStateProvider.currentStatus() } returns AuthStatus.GUEST

        // When / Then
        assertThatThrownBy { interceptor.intercept(chain) }
            .isInstanceOf(GuestModeNetworkBlockedException::class.java)
    }

    @Test
    fun `intercept should throw UnauthorizedNetworkBlockedException when status is UNAUTHORIZED`() {
        // Given
        val request = Request.Builder().url("https://api.example.com").build()
        every { chain.request() } returns request
        every { authStateProvider.currentStatus() } returns AuthStatus.UNAUTHORIZED

        // When / Then
        assertThatThrownBy { interceptor.intercept(chain) }
            .isInstanceOf(UnauthorizedNetworkBlockedException::class.java)
    }
}
