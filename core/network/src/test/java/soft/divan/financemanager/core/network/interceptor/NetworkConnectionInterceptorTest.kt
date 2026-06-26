package soft.divan.financemanager.core.network.interceptor

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import okhttp3.Interceptor
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test

class NetworkConnectionInterceptorTest {

    private val request = HttpTestFactory.request()
    private val connectivityManager = mockk<ConnectivityManager>()
    private val context = mockk<Context> {
        every { getSystemService(Context.CONNECTIVITY_SERVICE) } returns connectivityManager
    }

    private fun chain(): Interceptor.Chain {
        val chain = mockk<Interceptor.Chain>()
        every { chain.request() } returns request
        every { chain.proceed(request) } returns HttpTestFactory.response(request, 200)
        return chain
    }

    private fun stubConnectivity(
        network: Network? = mockk(),
        capabilities: NetworkCapabilities? = mockk(),
        hasInternet: Boolean = true
    ) {
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(any()) } returns capabilities
        capabilities?.let {
            every { it.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns hasInternet
        }
    }

    @Test
    fun `proceeds when connected to internet`() {
        stubConnectivity(hasInternet = true)
        val chain = chain()
        val interceptor = NetworkConnectionInterceptor(context)

        val result = interceptor.intercept(chain)

        assertThat(result.code).isEqualTo(200)
        verify(exactly = 1) { chain.proceed(request) }
    }

    @Test
    fun `throws when there is no active network`() {
        stubConnectivity(network = null, capabilities = null)
        val chain = chain()
        val interceptor = NetworkConnectionInterceptor(context)

        assertThatThrownBy { interceptor.intercept(chain) }
            .isInstanceOf(NoInternetException::class.java)
        verify(exactly = 0) { chain.proceed(any()) }
    }

    @Test
    fun `throws when capabilities are unavailable`() {
        stubConnectivity(capabilities = null)
        val interceptor = NetworkConnectionInterceptor(context)

        assertThatThrownBy { interceptor.intercept(chain()) }
            .isInstanceOf(NoInternetException::class.java)
    }

    @Test
    fun `throws when network lacks internet capability`() {
        stubConnectivity(hasInternet = false)
        val interceptor = NetworkConnectionInterceptor(context)

        assertThatThrownBy { interceptor.intercept(chain()) }
            .isInstanceOf(NoInternetException::class.java)
    }
}
