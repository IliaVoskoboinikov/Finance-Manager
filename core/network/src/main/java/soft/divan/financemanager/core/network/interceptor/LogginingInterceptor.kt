package soft.divan.financemanager.core.network.interceptor


import okhttp3.logging.HttpLoggingInterceptor
import soft.divan.financemanager.network.BuildConfig
import javax.inject.Inject


class LoggingInterceptor @Inject constructor() {
    fun provide(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }
}
