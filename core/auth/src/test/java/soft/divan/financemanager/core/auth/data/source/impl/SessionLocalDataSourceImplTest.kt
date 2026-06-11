package soft.divan.financemanager.core.auth.data.source.impl

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
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
import soft.divan.financemanager.core.auth.domain.model.AuthStatus

@OptIn(ExperimentalCoroutinesApi::class)
class SessionLocalDataSourceImplTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var dataSource: SessionLocalDataSourceImpl

    @Before
    fun setup() {
        dataStore = PreferenceDataStoreFactory.create(
            scope = TestScope(UnconfinedTestDispatcher()),
            produceFile = { temporaryFolder.newFile("test.preferences_pb") }
        )
        dataSource = SessionLocalDataSourceImpl(dataStore)
    }

    @Test
    fun `getAuthStatus should return UNAUTHORIZED by default`() = runTest {
        val status = dataSource.getAuthStatus().first()
        assertThat(status).isEqualTo(AuthStatus.UNAUTHORIZED)
    }

    @Test
    fun `setAuthStatus should update status`() = runTest {
        dataSource.setAuthStatus(AuthStatus.AUTHORIZED)

        val status = dataSource.getAuthStatus().first()
        assertThat(status).isEqualTo(AuthStatus.AUTHORIZED)
    }

    @Test
    fun `clearSession should reset status to UNAUTHORIZED`() = runTest {
        dataSource.setAuthStatus(AuthStatus.GUEST)
        dataSource.clearSession()

        val status = dataSource.getAuthStatus().first()
        assertThat(status).isEqualTo(AuthStatus.UNAUTHORIZED)
    }
}
