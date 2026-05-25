package soft.divan.financemanager.core.auth.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Authenticator
import okhttp3.Interceptor
import retrofit2.Retrofit
import soft.divan.financemanager.core.auth.data.api.AuthApiService
import soft.divan.financemanager.core.auth.data.authenticator.AuthManager
import soft.divan.financemanager.core.auth.data.authenticator.TokenAuthenticator
import soft.divan.financemanager.core.auth.data.interceptor.GuestAccessInterceptor
import soft.divan.financemanager.core.auth.data.source.SessionLocalDataSource
import soft.divan.financemanager.core.auth.data.source.TokenLocalDataSource
import soft.divan.financemanager.core.auth.data.source.impl.SessionLocalDataSourceImpl
import soft.divan.financemanager.core.auth.data.source.impl.TokenLocalDataSourceImpl
import soft.divan.financemanager.core.security.CryptoManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthProviderModule {

    @Provides
    @Singleton
    fun provideAuthApi(@BaseRetrofitQualifier retrofit: Retrofit): AuthApiService =
        retrofit.create(AuthApiService::class.java)

    @Provides
    @Singleton
    fun provideTokenAuthenticator(
        authManager: AuthManager
    ): Authenticator = TokenAuthenticator(authManager)

    @Provides
    @Singleton
    @GuestInterceptorQualifier
    fun provideGuestInterceptor(
        sessionDataSource: SessionLocalDataSource
    ): Interceptor = GuestAccessInterceptor(sessionDataSource)

    @Provides
    @Singleton
    @SessionDataStore
    fun provideSessionDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> = context.sessionDataStore

    @Provides
    @Singleton
    fun provideSessionPreferences(
        @SessionDataStore dataStore: DataStore<Preferences>
    ): SessionLocalDataSource = SessionLocalDataSourceImpl(dataStore)

    @Provides
    @Singleton
    @TokenDataStore
    fun provideTokenDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> = context.tokenDataStore

    @Provides
    @Singleton
    fun provideTokenPreferences(
        @TokenDataStore dataStore: DataStore<Preferences>,
        cryptoManager: CryptoManager
    ): TokenLocalDataSource = TokenLocalDataSourceImpl(dataStore, cryptoManager)
}
