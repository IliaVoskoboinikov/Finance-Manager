package soft.divan.financemanager.feature.security.impl.presenter.viewmodel

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import soft.divan.financemanager.core.domain.error.DomainError
import soft.divan.financemanager.core.domain.result.DomainResult
import soft.divan.financemanager.feature.security.impl.domain.usecase.SavePinUseCase
import soft.divan.financemanager.feature.security.impl.presenter.model.CreatePinError
import soft.divan.financemanager.feature.security.impl.presenter.model.CreatePinStep
import soft.divan.financemanager.feature.security.impl.presenter.model.CreatePinUiState
import java.security.GeneralSecurityException

@OptIn(ExperimentalCoroutinesApi::class)
class CreatePinViewModelTest {

    private val savePinUseCase = mockk<SavePinUseCase>()
    private lateinit var viewModel: CreatePinViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        coEvery { savePinUseCase(any()) } returns DomainResult.Success(Unit)
        viewModel = CreatePinViewModel(savePinUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `starts on the enter step with input enabled`() {
        assertThat(viewModel.uiState.value).isEqualTo(CreatePinUiState())
        assertThat(viewModel.uiState.value.isInputEnabled).isTrue()
    }

    @Test
    fun `first pin moves to the confirm step without saving`() {
        viewModel.onPinEntered(PIN)

        assertThat(viewModel.uiState.value.step).isEqualTo(CreatePinStep.CONFIRM)
        coVerify(exactly = 0) { savePinUseCase(any()) }
    }

    @Test
    fun `mismatched repeat starts over and shows the mismatch`() {
        viewModel.onPinEntered(PIN)
        viewModel.onPinEntered(OTHER_PIN)

        assertThat(viewModel.uiState.value)
            .isEqualTo(CreatePinUiState(step = CreatePinStep.ENTER, error = CreatePinError.MISMATCH))
        coVerify(exactly = 0) { savePinUseCase(any()) }
    }

    @Test
    fun `new first pin after a mismatch clears the message and is the one compared`() {
        viewModel.onPinEntered(PIN)
        viewModel.onPinEntered(OTHER_PIN)
        viewModel.onPinEntered(OTHER_PIN)

        assertThat(viewModel.uiState.value)
            .isEqualTo(CreatePinUiState(step = CreatePinStep.CONFIRM, error = null))

        viewModel.onPinEntered(OTHER_PIN)

        coVerify(exactly = 1) { savePinUseCase(OTHER_PIN) }
    }

    @Test
    fun `matching repeat saves the pin once and completes`() {
        viewModel.onPinEntered(PIN)
        viewModel.onPinEntered(PIN)

        coVerify(exactly = 1) { savePinUseCase(PIN) }
        assertThat(viewModel.uiState.value.isCompleted).isTrue()
        assertThat(viewModel.uiState.value.isSaving).isFalse()
        assertThat(viewModel.uiState.value.isInputEnabled).isFalse()
    }

    @Test
    fun `input while the pin is being saved is ignored`() {
        val saving = CompletableDeferred<DomainResult<Unit>>()
        coEvery { savePinUseCase(any()) } coAnswers { saving.await() }

        viewModel.onPinEntered(PIN)
        viewModel.onPinEntered(PIN)
        assertThat(viewModel.uiState.value.isSaving).isTrue()
        assertThat(viewModel.uiState.value.isInputEnabled).isFalse()

        viewModel.onPinEntered(PIN)
        viewModel.onPinEntered(PIN)
        saving.complete(DomainResult.Success(Unit))

        coVerify(exactly = 1) { savePinUseCase(any()) }
        assertThat(viewModel.uiState.value.isCompleted).isTrue()
    }

    @Test
    fun `input after the pin is saved is ignored while the screen closes`() {
        viewModel.onPinEntered(PIN)
        viewModel.onPinEntered(PIN)

        viewModel.onPinEntered(OTHER_PIN)
        viewModel.onPinEntered(OTHER_PIN)

        coVerify(exactly = 1) { savePinUseCase(any()) }
        assertThat(viewModel.uiState.value.step).isEqualTo(CreatePinStep.CONFIRM)
        assertThat(viewModel.uiState.value.isCompleted).isTrue()
    }

    @Test
    fun `failed save starts over with an error and allows a retry`() {
        coEvery { savePinUseCase(PIN) } returns
            DomainResult.Failure(DomainError.Unknown(GeneralSecurityException("keystore")))

        viewModel.onPinEntered(PIN)
        viewModel.onPinEntered(PIN)

        assertThat(viewModel.uiState.value)
            .isEqualTo(CreatePinUiState(step = CreatePinStep.ENTER, error = CreatePinError.SAVE_FAILED))
        assertThat(viewModel.uiState.value.isInputEnabled).isTrue()

        coEvery { savePinUseCase(PIN) } returns DomainResult.Success(Unit)
        viewModel.onPinEntered(PIN)

        // Первый ввод после сбоя — снова «придумать», а не подтверждение прежнего PIN
        assertThat(viewModel.uiState.value)
            .isEqualTo(CreatePinUiState(step = CreatePinStep.CONFIRM, error = null))
        coVerify(exactly = 1) { savePinUseCase(PIN) }

        viewModel.onPinEntered(PIN)

        coVerify(exactly = 2) { savePinUseCase(PIN) }
        assertThat(viewModel.uiState.value.isCompleted).isTrue()
    }

    private companion object {
        const val PIN = "1234"
        const val OTHER_PIN = "5678"
    }
}
