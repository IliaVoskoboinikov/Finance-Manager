package soft.divan.financemanager.core.auth.data.authenticator

import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import soft.divan.financemanager.core.auth.data.model.NetworkConstants.AUTH_HEADER
import soft.divan.financemanager.core.auth.data.model.NetworkConstants.BEARER_PREFIX
import javax.inject.Inject

/**
 * OkHttp Authenticator для автоматического обновления access token при получении HTTP 401.
 *
 * Данный класс перехватывает неавторизованные запросы (401 Unauthorized),
 * пытается обновить access token через refresh token и повторяет исходный запрос
 * с новым токеном.
 */
class TokenAuthenticator @Inject constructor(
    private val authManager: AuthManager
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        val retryCount = response.request().header(RETRY_HEADER)?.toIntOrNull() ?: 0
        if (retryCount >= MAX_RETRY_COUNT) return null

        // Токен, с которым был отправлен исходный запрос
        val oldToken = response.request().header(AUTH_HEADER)?.removePrefix(BEARER_PREFIX)

        val newToken = runBlocking {
            authManager.refreshTokenIfNeeded(oldToken)
        } ?: return null

        return response.request().newBuilder()
            .header(AUTH_HEADER, "$BEARER_PREFIX$newToken")
            .header(RETRY_HEADER, (retryCount + 1).toString())
            .build()
    }

    companion object {
        private const val RETRY_HEADER = "Retry-Attempt"
        private const val MAX_RETRY_COUNT = 5
    }
}
