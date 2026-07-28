package soft.divan.financemanager.core.auth.data.authenticator

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.io.IOException
import soft.divan.financemanager.core.auth.data.api.AuthApiService
import soft.divan.financemanager.core.auth.data.dto.AuthResponseDto
import soft.divan.financemanager.core.auth.domain.model.AuthEvent
import soft.divan.financemanager.core.auth.domain.model.SessionState
import soft.divan.financemanager.core.auth.domain.provider.AuthStateProvider
import javax.inject.Provider

class AuthManagerTest {

    private val authStateProvider = mockk<AuthStateProvider>(relaxed = true)
    private val authApiService = mockk<AuthApiService>()
    private val authApiProvider = Provider { authApiService }
    private lateinit var authManager: AuthManager

    @Before
    fun setup() {
        authManager = AuthManager(authStateProvider, authApiProvider)
    }

    @Test
    fun `getAccessToken should return token from state if authorized`() {
        every { authStateProvider.currentSessionState() } returns SessionState.Authorized(
            "token",
            "refresh"
        )

        val token = authManager.getAccessToken()

        assertThat(token).isEqualTo("token")
    }

    @Test
    fun `getAccessToken should return null if not authorized`() {
        every { authStateProvider.currentSessionState() } returns SessionState.Unauthorized

        val token = authManager.getAccessToken()

        assertThat(token).isNull()
    }

    @Test
    fun `refreshTokenIfNeeded should return new token on success`() = runTest {
        val oldToken = "old_token"
        val newToken = "new_token"
        val newRefresh = "new_refresh"

        every { authStateProvider.currentSessionState() } returns SessionState.Authorized(
            oldToken,
            "refresh"
        )
        coEvery { authApiService.refresh(any()) } returns Response.success(
            AuthResponseDto(accessToken = newToken, refreshToken = newRefresh)
        )

        val result = authManager.refreshTokenIfNeeded(oldToken)

        assertThat(result).isEqualTo(newToken)
        coVerify {
            authStateProvider.sendEvent(
                match {
                    it is AuthEvent.OnLoginSuccess &&
                        it.accessToken == newToken &&
                        it.refreshToken == newRefresh
                }
            )
        }
    }

    @Test
    fun `refreshTokenIfNeeded should return null and send OnSessionExpired on 401`() = runTest {
        val oldToken = "old_token"

        every { authStateProvider.currentSessionState() } returns SessionState.Authorized(
            oldToken,
            "refresh"
        )
        coEvery { authApiService.refresh(any()) } returns Response.error(401, mockk(relaxed = true))

        val result = authManager.refreshTokenIfNeeded(oldToken)

        assertThat(result).isNull()
        coVerify { authStateProvider.sendEvent(AuthEvent.OnSessionExpired) }
    }

    @Test
    fun `refreshTokenIfNeeded returns null when not authorized`() = runTest {
        every { authStateProvider.currentSessionState() } returns SessionState.Unauthorized

        assertThat(authManager.refreshTokenIfNeeded("old")).isNull()
        coVerify(exactly = 0) { authApiService.refresh(any()) }
    }

    @Test
    fun `refreshTokenIfNeeded returns current token when it was already refreshed`() = runTest {
        // токен в стейте уже отличается от oldToken — значит другой поток обновил его
        every { authStateProvider.currentSessionState() } returns
            SessionState.Authorized("fresh_token", "refresh")

        val result = authManager.refreshTokenIfNeeded("old_token")

        assertThat(result).isEqualTo("fresh_token")
        coVerify(exactly = 0) { authApiService.refresh(any()) }
    }

    @Test
    fun `refreshTokenIfNeeded rethrows IO exceptions`() = runTest {
        every { authStateProvider.currentSessionState() } returns
            SessionState.Authorized("old", "refresh")
        coEvery { authApiService.refresh(any()) } throws IOException("network down")

        val thrown = runCatching { authManager.refreshTokenIfNeeded("old") }.exceptionOrNull()

        assertThat(thrown).isInstanceOf(IOException::class.java)
    }

    @Test
    fun `refreshTokenIfNeeded returns null on non-IO exception`() = runTest {
        every { authStateProvider.currentSessionState() } returns
            SessionState.Authorized("old", "refresh")
        coEvery { authApiService.refresh(any()) } throws IllegalStateException("boom")

        assertThat(authManager.refreshTokenIfNeeded("old")).isNull()
        coVerify(exactly = 0) { authStateProvider.sendEvent(any()) }
    }

    @Test
    fun `refreshTokenIfNeeded returns null on server error without expiring session`() = runTest {
        every { authStateProvider.currentSessionState() } returns
            SessionState.Authorized("old", "refresh")
        coEvery { authApiService.refresh(any()) } returns Response.error(500, mockk(relaxed = true))

        assertThat(authManager.refreshTokenIfNeeded("old")).isNull()
        coVerify(exactly = 0) { authStateProvider.sendEvent(any()) }
    }
}
