package soft.divan.financemanager.core.auth.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import io.mockk.mockk
import okhttp3.OkHttpClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import soft.divan.financemanager.core.auth.data.authenticator.AuthManager
import soft.divan.financemanager.core.auth.data.authenticator.TokenAuthenticator
import soft.divan.financemanager.core.auth.data.interceptor.GuestAccessInterceptor
import soft.divan.financemanager.core.auth.data.source.impl.SessionLocalDataSourceImpl
import soft.divan.financemanager.core.auth.data.source.impl.TokenLocalDataSourceImpl
import soft.divan.financemanager.core.security.CryptoManager

class AuthProviderModuleTest {

    @Test
    fun `provideTokenAuthenticator creates project authenticator`() {
        val authenticator = AuthProviderModule.provideTokenAuthenticator(mockk<AuthManager>())

        assertThat(authenticator).isInstanceOf(TokenAuthenticator::class.java)
    }

    @Test
    fun `provideGuestInterceptor creates guest access interceptor`() {
        val interceptor = AuthProviderModule.provideGuestInterceptor { mockk() }

        assertThat(interceptor).isInstanceOf(GuestAccessInterceptor::class.java)
    }

    @Test
    fun `provideSessionPreferences builds datastore-backed source`() {
        val source = AuthProviderModule.provideSessionPreferences(
            mockk<DataStore<Preferences>>()
        )

        assertThat(source).isInstanceOf(SessionLocalDataSourceImpl::class.java)
    }

    @Test
    fun `provideTokenPreferences builds encrypted datastore source`() {
        val source = AuthProviderModule.provideTokenPreferences(
            mockk<DataStore<Preferences>>(),
            mockk<CryptoManager>()
        )

        assertThat(source).isInstanceOf(TokenLocalDataSourceImpl::class.java)
    }

    @Test
    fun `provideAuthApi creates AuthApiService from retrofit`() {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://localhost/")
            .client(OkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        assertThat(AuthProviderModule.provideAuthApi(retrofit)).isNotNull()
    }
}
