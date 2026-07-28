package soft.divan.financemanager.feature.haptics.impl.precenter.viewModel

import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import soft.divan.financemanager.feature.haptics.impl.domain.usecase.ObserveHapticsEnabledUseCase
import soft.divan.financemanager.feature.haptics.impl.domain.usecase.SetHapticsEnabledUseCase
import soft.divan.financemanager.feature.haptics.impl.precenter.model.HapticsUiState

@OptIn(ExperimentalCoroutinesApi::class)
class HapticsViewModelTest {

    private val setHapticsEnabledUseCase = mockk<SetHapticsEnabledUseCase>(relaxUnitFun = true)
    private val observeHapticsEnabledUseCase = mockk<ObserveHapticsEnabledUseCase>()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = HapticsViewModel(
        setHapticEnabledEnabledUseCase = setHapticsEnabledUseCase,
        observeHapticsEnabledUseCase = observeHapticsEnabledUseCase
    )

    @Test
    fun `load publishes Success with current setting`() = runTest {
        every { observeHapticsEnabledUseCase() } returns MutableStateFlow(true)

        val vm = viewModel()

        assertThat(vm.uiState.value).isEqualTo(HapticsUiState.Success(isEnabled = true))
    }

    @Test
    fun `state follows settings changes`() = runTest {
        val settings = MutableStateFlow(true)
        every { observeHapticsEnabledUseCase() } returns settings

        val vm = viewModel()
        settings.value = false

        assertThat(vm.uiState.first()).isEqualTo(HapticsUiState.Success(isEnabled = false))
    }

    @Test
    fun `load publishes Error when observation fails`() = runTest {
        every { observeHapticsEnabledUseCase() } returns
            flow { throw IllegalStateException("boom") }

        val vm = viewModel()

        assertThat(vm.uiState.value).isInstanceOf(HapticsUiState.Error::class.java)
    }

    @Test
    fun `setHapticEnabled delegates to use case`() = runTest {
        every { observeHapticsEnabledUseCase() } returns MutableStateFlow(true)

        viewModel().setHapticEnabled(false)

        coVerify(exactly = 1) { setHapticsEnabledUseCase(false) }
    }
}
