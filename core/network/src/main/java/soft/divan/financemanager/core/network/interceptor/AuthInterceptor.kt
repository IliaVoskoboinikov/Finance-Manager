package soft.divan.financemanager.core.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import soft.divan.financemanager.core.auth.data.authenticator.AuthManager
import soft.divan.financemanager.core.auth.data.model.NetworkConstants.AUTH_HEADER
import soft.divan.financemanager.core.auth.data.model.NetworkConstants.BEARER_PREFIX
import javax.inject.Inject
import javax.inject.Provider

class AuthInterceptor @Inject constructor(
    private val authManagerProvider: Provider<AuthManager>
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val token = authManagerProvider.get().getAccessToken()

        if (token.isNullOrEmpty()) {
            return chain.proceed(originalRequest)
        }

        val authenticatedRequest = originalRequest.newBuilder()
            .header(AUTH_HEADER, "$BEARER_PREFIX$token")
            .build()

        return chain.proceed(authenticatedRequest)
    }
}
