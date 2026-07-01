package soft.divan.financemanager.feature.security.impl.presenter.viewmodel

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import soft.divan.financemanager.feature.security.impl.domain.usecase.DeletePinUseCase
import soft.divan.financemanager.feature.security.impl.domain.usecase.IsPinSetUseCase
import soft.divan.financemanager.feature.security.impl.domain.usecase.VerifyPinUseCase
import soft.divan.financemanager.feature.security.impl.presenter.model.SecurityUiState

@OptIn(ExperimentalCoroutinesApi::class)
class SecurityViewModelTest {

    private val verifyPinUseCase = mockk<VerifyPinUseCase>()
    private val isPinSetUseCase = mockk<IsPinSetUseCase>()
    private val deletePinUseCase = mockk<DeletePinUseCase>(relaxed = true)

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() =
        SecurityViewModel(verifyPinUseCase, isPinSetUseCase, deletePinUseCase)

    @Test
    fun `uiState emits Success with hasPin true when a pin is set`() = runTest {
        every { isPinSetUseCase() } returns true

        val states = mutableListOf<SecurityUiState>()
        val job = launch(dispatcher) { viewModel().uiState.toList(states) }
        advanceUntilIdle()

        assertThat(states.last()).isEqualTo(SecurityUiState.Success(hasPin = true))
        job.cancel()
    }

    @Test
    fun `uiState emits Success with hasPin false when no pin is set`() = runTest {
        every { isPinSetUseCase() } returns false

        val states = mutableListOf<SecurityUiState>()
        val job = launch(dispatcher) { viewModel().uiState.toList(states) }
        advanceUntilIdle()

        assertThat(states.last()).isEqualTo(SecurityUiState.Success(hasPin = false))
        job.cancel()
    }

    @Test
    fun `verifyPin delegates to the use case`() {
        every { verifyPinUseCase(PIN) } returns true

        val result = viewModel().verifyPin(PIN)

        assertThat(result).isTrue()
        verify { verifyPinUseCase(PIN) }
    }

    @Test
    fun `deletePin deletes the pin and resets state to hasPin false`() = runTest {
        every { isPinSetUseCase() } returns true

        val vm = viewModel()
        val states = mutableListOf<SecurityUiState>()
        val job = launch(dispatcher) { vm.uiState.toList(states) }
        advanceUntilIdle()

        vm.deletePin()
        advanceUntilIdle()

        verify { deletePinUseCase() }
        assertThat(states.last()).isEqualTo(SecurityUiState.Success(hasPin = false))
        job.cancel()
    }

    private companion object {
        const val PIN = "1234"
    }
}
