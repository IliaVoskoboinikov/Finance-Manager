package soft.divan.financemanager.core.network.interceptor

import android.os.SystemClock
import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import kotlin.random.Random

/**
 * Повторяет запрос при 5xx-ошибках с экспоненциальным backoff и jitter.
 *
 * Фиксированная задержка заставляла всех клиентов повторять синхронно (thundering herd) и не
 * давала серверу времени восстановиться. Теперь задержка удваивается с каждой попыткой,
 * ограничивается [maxDelayMillis] и размывается случайной составляющей ("equal jitter").
 */
class RetryInterceptor(
    private val maxRetries: Int = DEFAULT_MAX_RETRIES,
    private val baseDelayMillis: Long = DEFAULT_BASE_DELAY_MS,
    private val maxDelayMillis: Long = DEFAULT_MAX_DELAY_MS,
    private val random: Random = Random.Default
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var attempt = 0
        var response: Response
        val request = chain.request()

        while (true) {
            response = chain.proceed(request)
            if (response.isSuccessful) return response

            if (response.code !in SERVER_ERROR_RANGE || attempt >= maxRetries) return response

            response.close()
            attempt++
            val delay = backoffDelayMillis(attempt)
            Log.d(TAG, "Retry attempt $attempt after error ${response.code}, waiting ${delay}ms")
            SystemClock.sleep(delay)
        }
    }

    /**
     * Задержка перед [attempt]-й попыткой: экспоненциальный рост базы, ограничение [maxDelayMillis]
     * и "equal jitter" — половина фиксирована, половина случайна, итог в `[capped/2, capped]`.
     */
    internal fun backoffDelayMillis(attempt: Int): Long {
        val exponent = (attempt - 1).coerceIn(0, MAX_BACKOFF_SHIFT)
        val capped = (baseDelayMillis shl exponent).coerceAtMost(maxDelayMillis)
        val half = capped / 2
        return half + random.nextLong(half + 1)
    }

    companion object {
        private const val DEFAULT_MAX_RETRIES = 3
        private const val DEFAULT_BASE_DELAY_MS = 500L
        private const val DEFAULT_MAX_DELAY_MS = 8_000L
        private const val MAX_BACKOFF_SHIFT = 16
        private const val TAG = "RetryInterceptor"
        private val SERVER_ERROR_RANGE = 500..599
    }
}
