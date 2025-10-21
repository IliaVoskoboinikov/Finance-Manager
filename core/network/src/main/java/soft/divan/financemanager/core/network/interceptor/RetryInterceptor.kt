package soft.divan.financemanager.core.network.interceptor

import android.os.SystemClock
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
            if (response.isSuccessful) return response

            if (response.code() !in 500..599 || attempt >= maxRetries) return response

            response.close()
            attempt++
            Log.d("RetryInterceptor", "Retry attempt $attempt after error ${response.code()}")
            SystemClock.sleep(delayMillis)
        }
    }
}
