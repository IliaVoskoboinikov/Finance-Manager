package soft.divan.financemanager.sync.domain.usecase.impl

import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import soft.divan.financemanager.sync.domain.repository.SyncRepository

class SyncUseCasesTest {

    private val repository = mockk<SyncRepository>(relaxUnitFun = true)

    @Test
    fun `ObserveLastSyncTimeUseCase delegates to repository`() = runTest {
        every { repository.observeLastSyncTime() } returns flowOf(42L)

        val useCase = ObserveLastSyncTimeUseCaseImpl(repository)

        assertThat(useCase().first()).isEqualTo(42L)
    }

    @Test
    fun `ObserveLastSyncTimeUseCase passes null through`() = runTest {
        every { repository.observeLastSyncTime() } returns flowOf(null)

        val useCase = ObserveLastSyncTimeUseCaseImpl(repository)

        assertThat(useCase().first()).isNull()
    }

    @Test
    fun `ObserveSyncIntervalHoursUseCase delegates to repository`() = runTest {
        every { repository.observeSyncIntervalHours() } returns flowOf(6)

        val useCase = ObserveSyncIntervalHoursUseCaseImpl(repository)

        assertThat(useCase().first()).isEqualTo(6)
    }

    @Test
    fun `ObserveSyncIntervalHoursUseCase passes null through`() = runTest {
        every { repository.observeSyncIntervalHours() } returns flowOf(null)

        val useCase = ObserveSyncIntervalHoursUseCaseImpl(repository)

        assertThat(useCase().first()).isNull()
    }

    @Test
    fun `SetLastSyncTimeUseCase delegates to repository`() = runTest {
        val useCase = SetLastSyncTimeUseCaseImpl(repository)

        useCase(123L)

        coVerify(exactly = 1) { repository.setLastSyncTime(123L) }
    }
}
