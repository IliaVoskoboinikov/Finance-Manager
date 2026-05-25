package soft.divan.financemanager.core.auth.data.interceptor

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import soft.divan.financemanager.core.auth.data.source.SessionLocalDataSource
import soft.divan.financemanager.core.auth.domain.model.AuthStatus
import java.io.IOException
import javax.inject.Inject

/**
 * Исключение, выбрасываемое при попытке сетевого доступа в режиме гостя или не авторизованного пользователя.
 */
class GuestModeException : IOException("Network access is prohibited for Guest or Unauthorized users.")

/**
 * Интерцептор-предохранитель.
 * Блокирует любые сетевые запросы (кроме авторизации), если пользователь находится в режиме Гостя.
 */
class GuestAccessInterceptor @Inject constructor(
    private val sessionDataSource: SessionLocalDataSource
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url().encodedPath()

        // Разрешаем только эндпоинты авторизации
        if (isAuthPath(path)) {
            return chain.proceed(request)
        }

        val status = runBlocking {
            sessionDataSource.getAuthStatus().first()
        }

        // Если гость или не авторизован - блокируем выход в сеть специальным исключением
        if (status == AuthStatus.GUEST || status == AuthStatus.UNAUTHORIZED) {
            throw GuestModeException()
        }

        return chain.proceed(request)
    }

    private fun isAuthPath(path: String): Boolean {
        return path.contains("/auth/login") ||
            path.contains("/auth/refresh") ||
            path.contains("/auth/register")
    }
}
