package soft.divan.financemanager.core.auth.data.source.impl

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import soft.divan.financemanager.core.security.CryptoManager
import java.security.GeneralSecurityException

@OptIn(ExperimentalCoroutinesApi::class)
class TokenLocalDataSourceImplTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private lateinit var dataStore: DataStore<Preferences>
    private val cryptoManager = mockk<CryptoManager>()
    private lateinit var dataSource: TokenLocalDataSourceImpl

    @Before
    fun setup() {
        dataStore = PreferenceDataStoreFactory.create(
            scope = TestScope(UnconfinedTestDispatcher()),
            produceFile = { temporaryFolder.newFile("test_tokens.preferences_pb") }
        )
        dataSource = TokenLocalDataSourceImpl(dataStore, cryptoManager)
    }

    @Test
    fun `updateRefreshToken encrypts and getRefreshToken decrypts it back`() = runTest {
        every { cryptoManager.encrypt("refresh123", any()) } returns "enc_refresh"
        every { cryptoManager.decrypt("enc_refresh", any()) } returns "refresh123"

        dataSource.updateRefreshToken("refresh123")

        assertThat(dataSource.getRefreshToken().first()).isEqualTo("refresh123")
    }

    @Test
    fun `getRefreshToken returns null when decryption fails`() = runTest {
        every { cryptoManager.encrypt("refresh123", any()) } returns "enc_refresh"
        every { cryptoManager.decrypt("enc_refresh", any()) } throws GeneralSecurityException("x")

        dataSource.updateRefreshToken("refresh123")

        assertThat(dataSource.getRefreshToken().first()).isNull()
    }

    @Test
    fun `updateAccessToken with null removes the stored access token`() = runTest {
        every { cryptoManager.encrypt("access", any()) } returns "enc_access"
        dataSource.updateAccessToken("access")

        dataSource.updateAccessToken(null)

        assertThat(dataSource.getAccessToken().first()).isNull()
    }

    @Test
    fun `updateRefreshToken with null removes the stored refresh token`() = runTest {
        every { cryptoManager.encrypt("refresh", any()) } returns "enc_refresh"
        dataSource.updateRefreshToken("refresh")

        dataSource.updateRefreshToken(null)

        assertThat(dataSource.getRefreshToken().first()).isNull()
    }

    @Test
    fun `getAccessToken should decrypt and return token`() = runTest {
        every { cryptoManager.encrypt("token123", "access_token_alias") } returns "encrypted_token"
        every { cryptoManager.decrypt("encrypted_token", "access_token_alias") } returns "token123"

        dataSource.updateAccessToken("token123")
        val token = dataSource.getAccessToken().first()

        assertThat(token).isEqualTo("token123")
    }

    @Test
    fun `getAccessToken should return null if decryption fails`() = runTest {
        every { cryptoManager.encrypt("token123", "access_token_alias") } returns "encrypted_token"
        every { cryptoManager.decrypt("encrypted_token", "access_token_alias") } throws Exception()

        dataSource.updateAccessToken("token123")
        val token = dataSource.getAccessToken().first()

        assertThat(token).isNull()
    }

    @Test
    fun `clearTokens should remove both tokens`() = runTest {
        every { cryptoManager.encrypt(any(), any()) } returns "encrypted"

        dataSource.updateAccessToken("access")
        dataSource.updateRefreshToken("refresh")
        dataSource.clearTokens()

        val accessToken = dataSource.getAccessToken().first()
        val refreshToken = dataSource.getRefreshToken().first()

        assertThat(accessToken).isNull()
        assertThat(refreshToken).isNull()
    }
}
