package soft.divan.financemanager.feature.sounds.impl

import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
import soft.divan.financemanager.feature.sounds.impl.domain.repository.SoundsRepository
import soft.divan.financemanager.feature.sounds.impl.domain.usecase.ObserveSoundsEnabledUseCase
import soft.divan.financemanager.feature.sounds.impl.domain.usecase.SetSoundsEnabledUseCase
import soft.divan.financemanager.feature.sounds.impl.domain.usecase.impl.ObserveSoundsEnabledUseCaseImpl
import soft.divan.financemanager.feature.sounds.impl.domain.usecase.impl.SetSoundsEnabledUseCaseImpl
import soft.divan.financemanager.feature.sounds.impl.precenter.model.SoundsUiState
import soft.divan.financemanager.feature.sounds.impl.precenter.viewModel.SoundViewModel

@OptIn(ExperimentalCoroutinesApi::class)
class SoundsUseCasesAndViewModelTest {

    /* ---------- use cases ---------- */

    private val repository = mockk<SoundsRepository>(relaxUnitFun = true)

    @Test
    fun `ObserveSoundsEnabledUseCase delegates to repository`() = runTest {
        every { repository.observeSoundsEnabled() } returns flowOf(true)

        assertThat(ObserveSoundsEnabledUseCaseImpl(repository)().first()).isTrue()
    }

    @Test
    fun `SetSoundsEnabledUseCase delegates to repository`() = runTest {
        SetSoundsEnabledUseCaseImpl(repository)(false)

        coVerify(exactly = 1) { repository.setSoundsEnabled(false) }
    }

    /* ---------- view model ---------- */

    private val setSoundsEnabledUseCase = mockk<SetSoundsEnabledUseCase>(relaxUnitFun = true)
    private val observeSoundsEnabledUseCase = mockk<ObserveSoundsEnabledUseCase>()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = SoundViewModel(
        setSoundsEnabledUseCase = setSoundsEnabledUseCase,
        observeSoundsEnabledUseCase = observeSoundsEnabledUseCase
    )

    @Test
    fun `state publishes current setting and follows changes`() = runTest {
        val settings = MutableStateFlow(true)
        every { observeSoundsEnabledUseCase() } returns settings

        val vm = viewModel()
        assertThat(vm.uiState.value).isEqualTo(SoundsUiState.Success(isEnabled = true))

        settings.value = false
        assertThat(vm.uiState.first()).isEqualTo(SoundsUiState.Success(isEnabled = false))
    }

    @Test
    fun `state publishes Error when observation fails`() = runTest {
        every { observeSoundsEnabledUseCase() } returns
            flow { throw IllegalStateException("boom") }

        val vm = viewModel()

        assertThat(vm.uiState.value).isInstanceOf(SoundsUiState.Error::class.java)
    }

    @Test
    fun `setSoundsEnabled delegates to use case`() = runTest {
        every { observeSoundsEnabledUseCase() } returns MutableStateFlow(true)

        viewModel().setSoundsEnabled(false)

        coVerify(exactly = 1) { setSoundsEnabledUseCase(false) }
    }
}
