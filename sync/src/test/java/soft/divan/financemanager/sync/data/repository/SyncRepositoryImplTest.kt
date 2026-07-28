package soft.divan.financemanager.sync.data.repository

import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import soft.divan.financemanager.sync.data.source.SyncLocalSource

class SyncRepositoryImplTest {

    private val localSource = mockk<SyncLocalSource>(relaxUnitFun = true)
    private val repository = SyncRepositoryImpl(localSource)

    @Test
    fun `observeLastSyncTime delegates to local source`() = runTest {
        every { localSource.observeLastSyncTime() } returns flowOf(42L)

        assertThat(repository.observeLastSyncTime().first()).isEqualTo(42L)
    }

    @Test
    fun `setLastSyncTime delegates to local source`() = runTest {
        repository.setLastSyncTime(123L)

        coVerify(exactly = 1) { localSource.setLastSyncTime(123L) }
    }

    @Test
    fun `observeSyncIntervalHours delegates to local source`() = runTest {
        every { localSource.observeSyncIntervalHours() } returns flowOf(6)

        assertThat(repository.observeSyncIntervalHours().first()).isEqualTo(6)
    }

    @Test
    fun `setSyncIntervalHours delegates to local source`() = runTest {
        repository.setSyncIntervalHours(8)

        coVerify(exactly = 1) { localSource.setSyncIntervalHours(8) }
    }
}
