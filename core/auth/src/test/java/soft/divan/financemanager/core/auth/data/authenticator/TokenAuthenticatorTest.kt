package soft.divan.financemanager.core.auth.data.authenticator

import io.mockk.coEvery
import io.mockk.mockk
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import soft.divan.financemanager.core.auth.data.model.NetworkConstants.AUTH_HEADER
import soft.divan.financemanager.core.auth.data.model.NetworkConstants.BEARER_PREFIX

class TokenAuthenticatorTest {

    private val authManager: AuthManager = mockk()
    private lateinit var authenticator: TokenAuthenticator

    @Before
    fun setUp() {
        authenticator = TokenAuthenticator(authManager)
    }

    @Test
    fun `authenticate should return request with new token when refresh successful`() {
        // Given
        val oldToken = "old_token"
        val newToken = "new_token"
        val request = Request.Builder()
            .url("https://api.example.com")
            .header(AUTH_HEADER, "$BEARER_PREFIX$oldToken")
            .build()
        val response = Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(401)
            .message("Unauthorized")
            .build()

        coEvery { authManager.refreshTokenIfNeeded(oldToken) } returns newToken

        // When
        val result = authenticator.authenticate(null, response)

        // Then
        assertThat(result).isNotNull
        assertThat(result?.header(AUTH_HEADER)).isEqualTo("$BEARER_PREFIX$newToken")
        assertThat(result?.header("Retry-Attempt")).isEqualTo("1")
    }

    @Test
    fun `authenticate should return null when refresh fails`() {
        // Given
        val oldToken = "old_token"
        val request = Request.Builder()
            .url("https://api.example.com")
            .header(AUTH_HEADER, "$BEARER_PREFIX$oldToken")
            .build()
        val response = Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(401)
            .message("Unauthorized")
            .build()

        coEvery { authManager.refreshTokenIfNeeded(oldToken) } returns null

        // When
        val result = authenticator.authenticate(null, response)

        // Then
        assertThat(result).isNull()
    }

    @Test
    fun `authenticate should return null when max retries reached`() {
        // Given
        val request = Request.Builder()
            .url("https://api.example.com")
            .header("Retry-Attempt", "5")
            .build()
        val response = Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(401)
            .message("Unauthorized")
            .build()

        // When
        val result = authenticator.authenticate(null, response)

        // Then
        assertThat(result).isNull()
    }
}
