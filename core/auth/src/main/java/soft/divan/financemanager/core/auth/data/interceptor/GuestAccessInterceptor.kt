package soft.divan.financemanager.core.auth.data.interceptor

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import soft.divan.financemanager.core.auth.domain.model.AuthStatus
import soft.divan.financemanager.core.auth.domain.provider.AuthStateProvider
import java.io.IOException
import javax.inject.Inject
import javax.inject.Provider

/**
 * Исключение, выбрасываемое при попытке сетевого доступа в режиме гостя.
 */
class GuestModeNetworkBlockedException : IOException("Network access is prohibited in guest mode.")

/**
 * Исключение, выбрасываемое при попытке сетевого доступа неавторизованного пользователя.
 */
class UnauthorizedNetworkBlockedException : IOException(
    "Network access is prohibited for unauthorized user."
)

/**
 * Интерцептор-предохранитель.
 * Блокирует любые сетевые запросы, если текущий статус авторизации не позволяет их выполнять.
 *
 * Особенности:
 * 1. Использует [Provider] для ленивой инициализации зависимостей.
 * 2. Поддерживает заголовок "X-Allow-Guest", позволяющий пропускать публичные запросы.
 */
class GuestAccessInterceptor @Inject constructor(
    private val authStateProvider: Provider<AuthStateProvider>
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        // Разрешаем запрос, если он помечен как публичный (доступный гостям)
        if (request.header(HEADER_ALLOW_GUEST) == "true") {
            return chain.proceed(request)
        }

        val status = authStateProvider.get().currentStatus()

        return when (status) {
            AuthStatus.AUTHORIZED -> chain.proceed(request)

            AuthStatus.GUEST -> {
                Log.w("NetworkSafety", "Request blocked: Guest mode is active for ${request.url}")
                throw GuestModeNetworkBlockedException()
            }

            AuthStatus.UNAUTHORIZED -> {
                Log.e("NetworkSafety", "Request blocked: User is UNAUTHORIZED for ${request.url}")
                throw UnauthorizedNetworkBlockedException()
            }
        }
    }

    companion object {
        /**
         * Заголовок для обхода блокировки гостевого режима.
         * Добавьте @Headers("X-Allow-Guest: true") к методу в Retrofit интерфейсе.
         */
        private const val HEADER_ALLOW_GUEST = "X-Allow-Guest"
    }
}
