package soft.divan.financemanager.data.network.interceptor


import okhttp3.logging.HttpLoggingInterceptor
import soft.divan.financemanager.BuildConfig
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
