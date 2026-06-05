package soft.divan.financemanager.core.auth.data.authenticator

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import soft.divan.financemanager.core.auth.data.api.AuthApiService
import soft.divan.financemanager.core.auth.data.dto.RefreshRequestDto
import soft.divan.financemanager.core.auth.domain.model.AuthEvent
import soft.divan.financemanager.core.auth.domain.model.SessionState
import soft.divan.financemanager.core.auth.domain.provider.AuthStateProvider
import java.io.IOException
import javax.inject.Inject
import javax.inject.Provider

/**
 * Менеджер авторизации, работающий поверх AuthStateProvider (Стейт-машины).
 * Отвечает за логику обновления токенов.
 */
class AuthManager @Inject constructor(
    private val authStateProvider: AuthStateProvider,
    private val authApiProvider: Provider<AuthApiService>
) {
    private val mutex = Mutex()

    /**
     * Возвращает текущий access token из атомарного стейта.
     */
    fun getAccessToken(): String? {
        val state = authStateProvider.currentSessionState()
        return (state as? SessionState.Authorized)?.accessToken
    }

    /**
     * Выполняет обновление токенов если нужно.
     */
    @Suppress("ReturnCount")
    suspend fun refreshTokenIfNeeded(oldToken: String?): String? {
        mutex.withLock {
            val state = authStateProvider.currentSessionState()

            // Если мы не в AUTHORIZED или токен уже обновлен - выходим
            val authState = state as? SessionState.Authorized ?: return null
            if (authState.accessToken != oldToken) {
                return authState.accessToken
            }

            val refreshToken = authState.refreshToken

            val response = try {
                authApiProvider.get().refresh(RefreshRequestDto(refreshToken))
            } catch (e: IOException) {
                throw e
            } catch (_: Exception) {
                return null
            }

            val body = response.body()
            return if (response.isSuccessful && body != null) {
                // Сообщаем стейт-машине об успехе (она сама обновит DataStore и StateFlow)
                authStateProvider.sendEvent(
                    AuthEvent.OnLoginSuccess(
                        accessToken = body.accessToken ?: "",
                        refreshToken = body.refreshToken ?: "",
                        shouldMergeData = true // При рефреше всегда мержим
                    )
                )
                body.accessToken
            } else {
                val code = response.code()
                // Если 401/403 - сессия протухла окончательно
                if (code == HTTP_UNAUTHORIZED || code == HTTP_FORBIDDEN) {
                    authStateProvider.sendEvent(AuthEvent.OnSessionExpired)
                }
                null
            }
        }
    }

    private companion object {
        const val HTTP_UNAUTHORIZED = 401
        const val HTTP_FORBIDDEN = 403
    }
}
