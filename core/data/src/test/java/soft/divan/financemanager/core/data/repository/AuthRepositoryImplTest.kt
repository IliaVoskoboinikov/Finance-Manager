package soft.divan.financemanager.core.data.repository

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import retrofit2.Response
import soft.divan.financemanager.core.auth.data.api.AuthApiService
import soft.divan.financemanager.core.auth.data.dto.AuthResponseDto
import soft.divan.financemanager.core.auth.domain.model.AuthEvent
import soft.divan.financemanager.core.auth.domain.provider.AuthStateProvider
import soft.divan.financemanager.core.domain.error.DomainError
import soft.divan.financemanager.core.domain.result.DomainResult
import soft.divan.financemanager.core.loggingerror.ErrorLogger

/**
 * Регресс-тесты на ключевую логику авторизации/регистрации.
 *
 * Главная цель — зафиксировать поведение при null/пустых токенах: сервер по контракту
 * (Swagger, accessToken/refreshToken nullable) может вернуть 200 без токенов, и такой ответ
 * НЕ должен считаться успешным входом (иначе пользователь застревает в статусе AUTHORIZED
 * с нерабочей сессией).
 */
class AuthRepositoryImplTest {

    private val authApi = mockk<AuthApiService>()
    private val authStateProvider = mockk<AuthStateProvider>(relaxed = true)
    private val errorLogger = mockk<ErrorLogger>(relaxed = true)

    private val repository = AuthRepositoryImpl(authApi, authStateProvider, errorLogger)

    private fun <T : Any> errorResponse(code: Int): Response<T> = mockk {
        every { isSuccessful } returns false
        every { this@mockk.code() } returns code
        every { message() } returns "error"
    }

    // region login

    @Test
    fun `login with valid tokens returns Success and emits OnLoginSuccess`() = runTest {
        coEvery { authApi.login(any()) } returns Response.success(
            AuthResponseDto(accessToken = "access", refreshToken = "refresh")
        )

        val result = repository.login("name", "pass", shouldMergeData = true)

        assertThat(result).isInstanceOf(DomainResult.Success::class.java)
        coVerify(exactly = 1) {
            authStateProvider.sendEvent(
                AuthEvent.OnLoginSuccess("access", "refresh", shouldMergeData = true)
            )
        }
    }

    @Test
    fun `login with null tokens returns Failure and does NOT open a session`() = runTest {
        coEvery { authApi.login(any()) } returns Response.success(
            AuthResponseDto(accessToken = null, refreshToken = null)
        )

        val result = repository.login("name", "pass", shouldMergeData = true)

        assertThat(result).isInstanceOf(DomainResult.Failure::class.java)
        coVerify(exactly = 0) { authStateProvider.sendEvent(any()) }
    }

    @Test
    fun `login with blank access token returns Failure`() = runTest {
        coEvery { authApi.login(any()) } returns Response.success(
            AuthResponseDto(accessToken = "  ", refreshToken = "refresh")
        )

        val result = repository.login("name", "pass", shouldMergeData = false)

        assertThat(result).isInstanceOf(DomainResult.Failure::class.java)
        coVerify(exactly = 0) { authStateProvider.sendEvent(any()) }
    }

    @Test
    fun `login propagates api failure as Failure without session`() = runTest {
        coEvery { authApi.login(any()) } returns errorResponse(401)

        val result = repository.login("name", "wrong", shouldMergeData = false)

        assertThat(result).isInstanceOf(DomainResult.Failure::class.java)
        assertThat((result as DomainResult.Failure).error).isEqualTo(DomainError.Unauthorized)
        coVerify(exactly = 0) { authStateProvider.sendEvent(any()) }
    }

    // endregion

    // region register

    @Test
    fun `register with valid tokens returns Success and emits OnLoginSuccess`() = runTest {
        coEvery { authApi.register(any()) } returns Response.success(
            AuthResponseDto(accessToken = "access", refreshToken = "refresh")
        )

        val result = repository.register("name", "pass", shouldMergeData = false)

        assertThat(result).isInstanceOf(DomainResult.Success::class.java)
        coVerify(exactly = 1) {
            authStateProvider.sendEvent(
                AuthEvent.OnLoginSuccess("access", "refresh", shouldMergeData = false)
            )
        }
    }

    @Test
    fun `register with null tokens returns Failure and does NOT open a session`() = runTest {
        coEvery { authApi.register(any()) } returns Response.success(
            AuthResponseDto(accessToken = null, refreshToken = null)
        )

        val result = repository.register("name", "pass", shouldMergeData = true)

        assertThat(result).isInstanceOf(DomainResult.Failure::class.java)
        coVerify(exactly = 0) { authStateProvider.sendEvent(any()) }
    }

    // endregion

    // region logout / guest

    @Test
    fun `logout always clears local session even if api call fails`() = runTest {
        coEvery { authApi.logout() } returns errorResponse(500)

        val result = repository.logout(shouldClearData = true)

        assertThat(result).isInstanceOf(DomainResult.Success::class.java)
        coVerify(exactly = 1) { authStateProvider.sendEvent(AuthEvent.OnLogout(true)) }
    }

    @Test
    fun `loginAsGuest emits OnEnterAsGuest`() = runTest {
        val result = repository.loginAsGuest()

        assertThat(result).isInstanceOf(DomainResult.Success::class.java)
        coVerify(exactly = 1) { authStateProvider.sendEvent(AuthEvent.OnEnterAsGuest) }
    }

    @Test
    fun `clearUserData emits OnClearData`() = runTest {
        val result = repository.clearUserData()

        assertThat(result).isInstanceOf(DomainResult.Success::class.java)
        coVerify(exactly = 1) { authStateProvider.sendEvent(AuthEvent.OnClearData) }
    }

    // endregion
}
