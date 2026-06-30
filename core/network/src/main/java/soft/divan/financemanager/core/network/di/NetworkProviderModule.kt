package soft.divan.financemanager.core.network.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Authenticator
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import soft.divan.financemanager.core.auth.data.authenticator.AuthManager
import soft.divan.financemanager.core.auth.di.BaseRetrofitQualifier
import soft.divan.financemanager.core.auth.di.GuestInterceptorQualifier
import soft.divan.financemanager.core.network.BuildConfig
import soft.divan.financemanager.core.network.interceptor.AuthInterceptor
import soft.divan.financemanager.core.network.interceptor.LoggingInterceptor
import soft.divan.financemanager.core.network.interceptor.NetworkConnectionInterceptor
import soft.divan.financemanager.core.network.interceptor.RetryInterceptor
import soft.divan.financemanager.core.network.util.ConnectivityManagerNetworkMonitor
import soft.divan.financemanager.core.network.util.NetworkMonitor
import java.io.File
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkProviderModule {

    private const val CACHE_SIZE_MB = 10L // 10 MB
    private const val BYTES_IN_KB = 1024L
    private const val KB_IN_MB = 1024L

    @Provides
    @Singleton
    fun provideCache(@ApplicationContext context: Context): Cache {
        val cacheSize = CACHE_SIZE_MB * BYTES_IN_KB * KB_IN_MB // bytes
        val cacheDir = File(context.cacheDir, "http_cache")
        return Cache(cacheDir, cacheSize)
    }
// todo
    /* По умолчанию OkHttp будет кэшировать только те ответы, где сервер возвращает
     корректные заголовки Cache-Control или Expires.
    Пример серверного ответа, который будет кэшироваться:
        Cache-Control: public, max-age=3600
     */

    @Provides
    @Singleton
    @AuthInterceptorQualifier
    fun provideAuthInterceptor(
        authManagerProvider: Provider<AuthManager>
    ): Interceptor = AuthInterceptor(authManagerProvider)

    @Provides
    @Singleton
    @NetworkInterceptorQualifier
    fun provideNetworkInterceptor(@ApplicationContext context: Context): Interceptor =
        NetworkConnectionInterceptor(context)

    @Provides
    @Singleton
    @RetryInterceptorQualifier
    fun provideRetryInterceptor(): Interceptor = RetryInterceptor()

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(
        provider: LoggingInterceptor
    ): HttpLoggingInterceptor = provider.provide()

    /**
     * Базовый OkHttpClient без Authenticator и AuthInterceptor.
     * Используется для запросов авторизации (Login, Refresh).
     */
    @Provides
    @Singleton
    @BaseOkHttpClientQualifier
    fun provideBaseOkHttpClient(
        @NetworkInterceptorQualifier network: Interceptor,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(network)
        .addInterceptor(loggingInterceptor)
        .build()

    /**
     * Основной OkHttpClient со всеми перехватчиками и автоматическим обновлением токена.
     */
    @Suppress("LongParameterList")
    @Provides
    @Singleton
    fun provideOkHttpClient(
        @AuthInterceptorQualifier auth: Interceptor,
        @NetworkInterceptorQualifier network: Interceptor,
        @RetryInterceptorQualifier retry: Interceptor,
        @GuestInterceptorQualifier guest: Interceptor,
        loggingInterceptor: HttpLoggingInterceptor,
        authenticator: Authenticator,
        cache: Cache
    ): OkHttpClient = OkHttpClient.Builder()
        .cache(cache)
        .addInterceptor(network)
        .addInterceptor(guest)
        .addInterceptor(auth)
        .addInterceptor(retry)
        .addInterceptor(loggingInterceptor)
        .authenticator(authenticator)
        .build()

    /**
     * Базовый Retrofit для Auth API.
     */
    @Provides
    @Singleton
    @BaseRetrofitQualifier
    fun provideBaseRetrofit(
        @BaseOkHttpClientQualifier okHttp: OkHttpClient
    ): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.HOST)
        .client(okHttp)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    /**
     * Основной Retrofit для всех остальных API.
     */
    @Provides
    @Singleton
    fun provideRetrofit(okHttp: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.HOST)
        .client(okHttp)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    @Singleton
    fun provideNetworkMonitor(
        networkMonitor: ConnectivityManagerNetworkMonitor
    ): NetworkMonitor = networkMonitor
}
