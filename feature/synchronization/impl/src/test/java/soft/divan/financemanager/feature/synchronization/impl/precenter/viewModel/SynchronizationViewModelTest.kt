package soft.divan.financemanager.feature.synchronization.impl.precenter.viewModel

import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import soft.divan.financemanager.core.domain.utli.UiDateFormatter
import soft.divan.financemanager.feature.synchronization.impl.precenter.model.SynchronizationUiState
import soft.divan.financemanager.sync.domain.usecase.ObserveLastSyncTimeUseCase
import soft.divan.financemanager.sync.domain.usecase.ObserveSyncIntervalHoursUseCase
import soft.divan.financemanager.sync.domain.usecase.SetSyncIntervalHoursUseCase
import soft.divan.financemanager.sync.worker.SYNCHRONIZATION_PERIOD_IN_HOURS
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class SynchronizationViewModelTest {

    private val setSyncIntervalHoursUseCase = mockk<SetSyncIntervalHoursUseCase>(relaxUnitFun = true)
    private val observeLastSyncTimeUseCase = mockk<ObserveLastSyncTimeUseCase>()
    private val observeSyncIntervalHoursUseCase = mockk<ObserveSyncIntervalHoursUseCase>()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = SynchronizationViewModel(
        setSyncIntervalHoursUseCase = setSyncIntervalHoursUseCase,
        observeLastSyncTimeUseCase = observeLastSyncTimeUseCase,
        observeSyncIntervalHoursUseCase = observeSyncIntervalHoursUseCase
    )

    @Test
    fun `state combines last sync time and interval`() = runTest {
        val syncMillis = 1_700_000_000_000L
        every { observeLastSyncTimeUseCase() } returns flowOf(syncMillis)
        every { observeSyncIntervalHoursUseCase() } returns flowOf(8)

        val vm = viewModel()
        val state = vm.uiState.first { it !is SynchronizationUiState.Loading }

        val success = state as SynchronizationUiState.Success
        assertThat(success.hoursInterval).isEqualTo(8)
        assertThat(success.lastSyncTime).isEqualTo(
            UiDateFormatter.formatDateTime(Instant.ofEpochMilli(syncMillis))
        )
    }

    @Test
    fun `state defaults when nothing synced yet`() = runTest {
        every { observeLastSyncTimeUseCase() } returns flowOf(null)
        every { observeSyncIntervalHoursUseCase() } returns flowOf(null)

        val vm = viewModel()
        val state = vm.uiState.first { it !is SynchronizationUiState.Loading }

        val success = state as SynchronizationUiState.Success
        assertThat(success.lastSyncTime).isNull()
        assertThat(success.hoursInterval).isEqualTo(SYNCHRONIZATION_PERIOD_IN_HOURS)
    }

    @Test
    fun `state publishes Error when observation fails`() = runTest {
        every { observeLastSyncTimeUseCase() } returns
            flow { throw IllegalStateException("boom") }
        every { observeSyncIntervalHoursUseCase() } returns flowOf(4)

        val vm = viewModel()
        val state = vm.uiState.first { it !is SynchronizationUiState.Loading }

        assertThat(state).isInstanceOf(SynchronizationUiState.Error::class.java)
    }

    @Test
    fun `onIntervalChanged delegates to use case`() = runTest {
        every { observeLastSyncTimeUseCase() } returns flowOf(null)
        every { observeSyncIntervalHoursUseCase() } returns flowOf(null)

        viewModel().onIntervalChanged(12)

        coVerify(exactly = 1) { setSyncIntervalHoursUseCase(12) }
    }

    @Test
    fun `toDateTimeString formats epoch millis in system zone`() {
        val millis = 1_700_000_000_000L

        assertThat(millis.toDateTimeString())
            .isEqualTo(UiDateFormatter.formatDateTime(Instant.ofEpochMilli(millis)))
    }
}
