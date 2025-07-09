package soft.divan.financemanager.core.network.interceptor

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response

class RetryInterceptor(
    private val maxRetries: Int = 3,
    private val delayMillis: Long = 2000
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var attempt = 0
        var response: Response
        val request = chain.request()

        while (true) {
            response = chain.proceed(request)
            if (!response.isSuccessful && response.code() == 500 && attempt < maxRetries) {
                Log.d("RetryInterceptor", "Retry attempt $attempt")
                attempt++
                Thread.sleep(delayMillis)
                continue
            }
            return response
        }
    }
}
