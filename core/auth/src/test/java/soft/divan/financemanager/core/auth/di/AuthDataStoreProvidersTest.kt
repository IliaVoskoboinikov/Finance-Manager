package soft.divan.financemanager.core.auth.di

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import soft.divan.financemanager.core.security.CryptoManager

/**
 * Robolectric-тесты DataStore-провайдеров: они обращаются к Context-делегатам
 * [Context.sessionDataStore]/[Context.tokenDataStore], которые нельзя инициализировать без
 * реального Context.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AuthDataStoreProvidersTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `provideSessionDataStore returns a datastore`() {
        assertThat(AuthProviderModule.provideSessionDataStore(context)).isNotNull()
    }

    @Test
    fun `provideTokenDataStore returns a datastore`() {
        assertThat(AuthProviderModule.provideTokenDataStore(context)).isNotNull()
    }

    @Test
    fun `provideTokenPreferences wires token datastore and crypto`() {
        val source = AuthProviderModule.provideTokenPreferences(
            AuthProviderModule.provideTokenDataStore(context),
            mockk<CryptoManager>()
        )

        assertThat(source).isNotNull()
    }
}
