package soft.divan.financemanager.core.network.di

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.logging.HttpLoggingInterceptor
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import soft.divan.financemanager.core.network.BuildConfig
import soft.divan.financemanager.core.network.interceptor.AuthInterceptor
import soft.divan.financemanager.core.network.interceptor.LoggingInterceptor
import soft.divan.financemanager.core.network.interceptor.NetworkConnectionInterceptor
import soft.divan.financemanager.core.network.interceptor.RetryInterceptor
import soft.divan.financemanager.core.network.util.ConnectivityManagerNetworkMonitor

class NetworkProviderModuleTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val authInterceptor = mockk<Interceptor>()
    private val networkInterceptor = mockk<Interceptor>()
    private val retryInterceptor = mockk<Interceptor>()
    private val guestInterceptor = mockk<Interceptor>()
    private val loggingInterceptor = HttpLoggingInterceptor()
    private val authenticator = mockk<Authenticator>()

    @Test
    fun `provideCache creates 10MB cache in http_cache dir`() {
        val context = mockk<Context> {
            every { cacheDir } returns tempFolder.root
        }

        val cache = NetworkProviderModule.provideCache(context)

        assertThat(cache.maxSize()).isEqualTo(10L * 1024 * 1024)
        assertThat(cache.directory.name).isEqualTo("http_cache")
    }

    @Test
    fun `interceptor factories create project interceptors`() {
        assertThat(NetworkProviderModule.provideAuthInterceptor { mockk() })
            .isInstanceOf(AuthInterceptor::class.java)
        assertThat(NetworkProviderModule.provideNetworkInterceptor(mockk()))
            .isInstanceOf(NetworkConnectionInterceptor::class.java)
        assertThat(NetworkProviderModule.provideRetryInterceptor())
            .isInstanceOf(RetryInterceptor::class.java)
    }

    @Test
    fun `provideHttpLoggingInterceptor delegates to LoggingInterceptor`() {
        val provided = NetworkProviderModule.provideHttpLoggingInterceptor(LoggingInterceptor())

        assertThat(provided).isInstanceOf(HttpLoggingInterceptor::class.java)
    }

    @Test
    fun `base client contains only network and logging interceptors`() {
        val client = NetworkProviderModule.provideBaseOkHttpClient(
            network = networkInterceptor,
            loggingInterceptor = loggingInterceptor
        )

        assertThat(client.interceptors).containsExactly(networkInterceptor, loggingInterceptor)
        assertThat(client.authenticator).isEqualTo(Authenticator.NONE)
        assertThat(client.cache).isNull()
    }

    @Test
    fun `main client wires interceptors in safety order with authenticator and cache`() {
        val context = mockk<Context> { every { cacheDir } returns tempFolder.root }
        val cache = NetworkProviderModule.provideCache(context)

        val client = NetworkProviderModule.provideOkHttpClient(
            auth = authInterceptor,
            network = networkInterceptor,
            retry = retryInterceptor,
            guest = guestInterceptor,
            loggingInterceptor = loggingInterceptor,
            authenticator = authenticator,
            cache = cache
        )

        // network → guest → auth → retry → logging: гость блокируется до авторизации,
        // ретраи идут уже с токеном
        assertThat(client.interceptors).containsExactly(
            networkInterceptor,
            guestInterceptor,
            authInterceptor,
            retryInterceptor,
            loggingInterceptor
        )
        assertThat(client.authenticator).isSameAs(authenticator)
        assertThat(client.cache).isSameAs(cache)
    }

    @Test
    fun `provideNetworkMonitor returns the connectivity-backed monitor`() {
        val monitor = mockk<ConnectivityManagerNetworkMonitor>()

        assertThat(NetworkProviderModule.provideNetworkMonitor(monitor)).isSameAs(monitor)
    }

    @Test
    fun `base and main retrofit use configured host`() {
        val client = NetworkProviderModule.provideBaseOkHttpClient(
            network = networkInterceptor,
            loggingInterceptor = loggingInterceptor
        )
        val gson = SerializationModule.provideGson()

        val baseRetrofit = NetworkProviderModule.provideBaseRetrofit(client, gson)
        val mainRetrofit = NetworkProviderModule.provideRetrofit(client, gson)

        assertThat(baseRetrofit.baseUrl().toString()).isEqualTo(BuildConfig.HOST)
        assertThat(mainRetrofit.baseUrl().toString()).isEqualTo(BuildConfig.HOST)
    }
}
