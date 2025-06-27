package soft.divan.financemanager.data.network.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Named
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import soft.divan.financemanager.BuildConfig
import soft.divan.financemanager.data.network.api.AccountApiService
import soft.divan.financemanager.data.network.api.TransactionApiService
import soft.divan.financemanager.data.network.interceptor.AuthInterceptor
import soft.divan.financemanager.data.network.interceptor.LoggingInterceptor
import soft.divan.financemanager.data.network.interceptor.NetworkConnectionInterceptor
import soft.divan.financemanager.data.network.interceptor.RetryInterceptor

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Named("authInterceptor")
    @Provides
    fun provideAuthInterceptor(): Interceptor = AuthInterceptor { BuildConfig.API_TOKEN }

    @Named("networkInterceptor")
    @Provides
    fun provideNetworkInterceptor(@ApplicationContext context: Context): Interceptor =
        NetworkConnectionInterceptor(context)

    @Named("retryInterceptor")
    @Provides
    fun provideRetryInterceptor(): Interceptor = RetryInterceptor()

    @Provides
    fun provideHttpLoggingInterceptor(
        provider: LoggingInterceptor
    ): HttpLoggingInterceptor = provider.provide()


    @Provides
    fun provideOkHttpClient(
        @Named("authInterceptor") auth: Interceptor,
        @Named("networkInterceptor") network: Interceptor,
        @Named("retryInterceptor") retry: Interceptor,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(network)
        .addInterceptor(auth)
        .addInterceptor(retry)
        .addInterceptor(loggingInterceptor)
        .build()

    @Provides
    fun provideRetrofit(okHttp: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.HOST)
        .client(okHttp)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    fun provideAccountApi(retrofit: Retrofit): AccountApiService =
        retrofit.create(AccountApiService::class.java)

    @Provides
    fun provideTransactionApi(retrofit: Retrofit): TransactionApiService =
        retrofit.create(TransactionApiService::class.java)

}
