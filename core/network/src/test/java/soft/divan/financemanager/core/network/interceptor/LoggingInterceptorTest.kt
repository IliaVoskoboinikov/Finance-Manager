package soft.divan.financemanager.core.network.interceptor

import okhttp3.logging.HttpLoggingInterceptor
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import soft.divan.financemanager.core.network.BuildConfig

class LoggingInterceptorTest {

    @Test
    fun `provide returns a non-null interceptor`() {
        assertThat(LoggingInterceptor().provide()).isNotNull()
    }

    @Test
    fun `level reflects the build type`() {
        val expected = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }

        assertThat(LoggingInterceptor().provide().level).isEqualTo(expected)
    }
}
