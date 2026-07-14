package soft.divan.financemanager.feature.security.impl.presenter.viewmodel

import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import soft.divan.financemanager.feature.security.impl.domain.usecase.SavePinUseCase
import soft.divan.financemanager.feature.security.impl.presenter.model.CreatePinScreenState

class CreatePinViewModelTest {

    private val savePinUseCase = mockk<SavePinUseCase>(relaxUnitFun = true)
    private val viewModel = CreatePinViewModel(savePinUseCase)

    @Test
    fun `initial state is InitialState`() {
        assertThat(viewModel.uiState.value).isEqualTo(CreatePinScreenState.InitialState)
    }

    @Test
    fun `changeState publishes new screen state`() {
        viewModel.changeState(CreatePinScreenState.EnteringPinState("12"))

        assertThat(viewModel.uiState.value)
            .isEqualTo(CreatePinScreenState.EnteringPinState("12"))
    }

    @Test
    fun `savePinCode stores pin and moves to PinCreatedState`() {
        viewModel.savePinCode("1234")

        verify(exactly = 1) { savePinUseCase("1234") }
        assertThat(viewModel.uiState.value).isEqualTo(CreatePinScreenState.PinCreatedState)
    }

    @Test
    fun `state flow walks through pin creation steps`() {
        viewModel.changeState(CreatePinScreenState.EnteringPinState("1234"))
        viewModel.changeState(CreatePinScreenState.ConfirmingPinState("1234"))
        viewModel.savePinCode("1234")

        assertThat(viewModel.uiState.value).isEqualTo(CreatePinScreenState.PinCreatedState)
    }

    @Test
    fun `error state carries message`() {
        viewModel.changeState(CreatePinScreenState.ErrorState("mismatch"))

        assertThat(viewModel.uiState.value).isEqualTo(CreatePinScreenState.ErrorState("mismatch"))
    }
}
