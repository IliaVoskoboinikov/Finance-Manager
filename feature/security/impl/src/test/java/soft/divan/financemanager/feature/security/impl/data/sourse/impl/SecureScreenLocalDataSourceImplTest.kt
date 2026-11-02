package soft.divan.financemanager.feature.security.impl.data.sourse.impl

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import soft.divan.financemanager.feature.security.impl.data.repository.SecureScreenRepositoryImpl
import soft.divan.financemanager.feature.security.impl.data.sourse.SecureScreenLocalDataSource
import java.io.File

class SecureScreenLocalDataSourceImplTest {

    @get:Rule
    val folder = TemporaryFolder()

    private val scope = CoroutineScope(SupervisorJob())
    private val dataSource by lazy {
        SecureScreenLocalDataSourceImpl(
            PreferenceDataStoreFactory.create(scope = scope) {
                File(folder.root, "security_settings.preferences_pb")
            }
        )
    }

    @After
    fun tearDown() {
        scope.cancel()
    }

    @Test
    fun `screen is protected by default`() = runTest {
        assertThat(dataSource.observeEnabled().first()).isTrue()
    }

    @Test
    fun `user choice is persisted`() = runTest {
        dataSource.setEnabled(false)
        assertThat(dataSource.observeEnabled().first()).isFalse()

        dataSource.setEnabled(true)
        assertThat(dataSource.observeEnabled().first()).isTrue()
    }

    @Test
    fun `repository delegates to the data source`() = runTest {
        val source = mockk<SecureScreenLocalDataSource>(relaxUnitFun = true) {
            every { observeEnabled() } returns flowOf(false)
        }
        val repository = SecureScreenRepositoryImpl(source)

        assertThat(repository.observeEnabled().first()).isFalse()
        repository.setEnabled(true)
        coVerify { source.setEnabled(true) }
    }
}
