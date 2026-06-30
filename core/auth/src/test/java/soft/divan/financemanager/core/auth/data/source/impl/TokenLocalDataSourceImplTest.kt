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
