package soft.divan.financemanager.core.data.util.safeCall

import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import retrofit2.Response
import soft.divan.financemanager.core.auth.data.interceptor.GuestModeNetworkBlockedException
import soft.divan.financemanager.core.auth.data.interceptor.UnauthorizedNetworkBlockedException
import soft.divan.financemanager.core.domain.error.DomainError
import soft.divan.financemanager.core.domain.result.DomainResult
import soft.divan.financemanager.core.loggingerror.ErrorLogger
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.coroutines.EmptyCoroutineContext

class SafeApiCallTest {

    private val errorLogger: ErrorLogger = mockk(relaxed = true)

    private suspend inline fun <reified T : Any> call(
        noinline block: suspend () -> Response<T>
    ): DomainResult<T> = safeApiCall(errorLogger, EmptyCoroutineContext, block)

    @Test
    fun `successful response with body returns Success`() = runTest {
        val result = call { Response.success("payload") }

        assertThat(result).isEqualTo(DomainResult.Success("payload"))
    }

    @Test
    fun `successful response without body returns Success for Unit endpoints`() = runTest {
        val result = call<Unit> { Response.success(null) }

        assertThat(result).isEqualTo(DomainResult.Success(Unit))
    }

    @Test
    fun `successful response without body returns NoData for typed endpoints`() = runTest {
        val result = call<String> { Response.success(null) }

        assertThat(result).isEqualTo(DomainResult.Failure(DomainError.NoData))
        verify { errorLogger.recordError(match<String> { it.contains("Empty body") }) }
    }

    @Test
    fun `http 404 maps to NoData`() = runTest {
        val result = call<String> { Response.error(404, "".toResponseBody()) }

        assertThat(result).isEqualTo(DomainResult.Failure(DomainError.NoData))
    }

    @Test
    fun `http 401 maps to Unauthorized`() = runTest {
        val result = call<String> { Response.error(401, "".toResponseBody()) }

        assertThat(result).isEqualTo(DomainResult.Failure(DomainError.Unauthorized))
    }

    @Test
    fun `http 500 maps to Unknown`() = runTest {
        val result = call<String> { Response.error(500, "".toResponseBody()) }

        assertThat(result).isEqualTo(DomainResult.Failure(DomainError.Unknown(null)))
    }

    @Test
    fun `http 599 maps to Unknown as server error`() = runTest {
        val result = call<String> { Response.error(599, "".toResponseBody()) }

        assertThat(result).isEqualTo(DomainResult.Failure(DomainError.Unknown(null)))
    }

    @Test
    fun `unexpected http code maps to Unknown with cause`() = runTest {
        val result = call<String> { Response.error(418, "".toResponseBody()) }

        val failure = result as DomainResult.Failure
        assertThat(failure.error).isInstanceOf(DomainError.Unknown::class.java)
    }

    @Test
    fun `http failures are logged`() = runTest {
        call<String> { Response.error(500, "".toResponseBody()) }

        verify { errorLogger.recordError(match<String> { it.contains("HTTP 500") }) }
    }

    @Test
    fun `UnknownHostException maps to NetworkUnavailable`() = runTest {
        val result = call<String> { throw UnknownHostException("host") }

        assertThat(result).isEqualTo(DomainResult.Failure(DomainError.NetworkUnavailable))
    }

    @Test
    fun `ConnectException maps to NetworkUnavailable`() = runTest {
        val result = call<String> { throw ConnectException("refused") }

        assertThat(result).isEqualTo(DomainResult.Failure(DomainError.NetworkUnavailable))
    }

    @Test
    fun `SocketTimeoutException maps to NetworkUnavailable`() = runTest {
        val result = call<String> { throw SocketTimeoutException("timeout") }

        assertThat(result).isEqualTo(DomainResult.Failure(DomainError.NetworkUnavailable))
    }

    @Test
    fun `guest mode block maps to GuestModeBlocked`() = runTest {
        val result = call<String> { throw GuestModeNetworkBlockedException() }

        assertThat(result).isEqualTo(DomainResult.Failure(DomainError.GuestModeBlocked))
    }

    @Test
    fun `unauthorized block maps to Unauthorized`() = runTest {
        val result = call<String> { throw UnauthorizedNetworkBlockedException() }

        assertThat(result).isEqualTo(DomainResult.Failure(DomainError.Unauthorized))
    }

    @Test
    fun `unexpected exception maps to Unknown preserving cause and is logged`() = runTest {
        val boom = IllegalStateException("boom")

        val result = call<String> { throw boom }

        assertThat(result).isEqualTo(DomainResult.Failure(DomainError.Unknown(boom)))
        verify { errorLogger.recordError(boom) }
    }

    @Test
    fun `network exceptions are not sent to error logger`() = runTest {
        call<String> { throw UnknownHostException("host") }

        verify(exactly = 0) { errorLogger.recordError(any<Throwable>()) }
        verify(exactly = 0) { errorLogger.recordError(any<String>()) }
    }
}
