package soft.divan.financemanager.core.network.interceptor

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.io.IOException

class NoInternetExceptionTest {

    @Test
    fun `has default message`() {
        assertThat(NoInternetException().message).isEqualTo("No internet connection")
    }

    @Test
    fun `supports custom message`() {
        assertThat(NoInternetException("offline").message).isEqualTo("offline")
    }

    @Test
    fun `is an IOException so OkHttp treats it as a transport failure`() {
        assertThat(NoInternetException()).isInstanceOf(IOException::class.java)
    }
}
