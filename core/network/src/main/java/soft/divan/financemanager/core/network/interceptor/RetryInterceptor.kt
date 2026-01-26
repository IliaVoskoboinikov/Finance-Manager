package soft.divan.financemanager.core.network.interceptor

import android.os.SystemClock
import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response

class RetryInterceptor(
    private val maxRetries: Int = DEFAULT_MAX_RETRIES,
    private val delayMillis: Long = DEFAULT_RETRY_DELAY_MS
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var attempt = 0
        var response: Response
        val request = chain.request()

        while (true) {
            response = chain.proceed(request)
            if (response.isSuccessful) return response

            if (response.code() !in SERVER_ERROR_RANGE || attempt >= maxRetries) return response

            response.close()
            attempt++
            Log.d(TAG, "Retry attempt $attempt after error ${response.code()}")
            SystemClock.sleep(delayMillis)
        }
    }

    companion object {
        private const val DEFAULT_MAX_RETRIES = 3
        private const val DEFAULT_RETRY_DELAY_MS = 2_000L
        private const val TAG = "RetryInterceptor"
        private val SERVER_ERROR_RANGE = 500..599
    }
}
