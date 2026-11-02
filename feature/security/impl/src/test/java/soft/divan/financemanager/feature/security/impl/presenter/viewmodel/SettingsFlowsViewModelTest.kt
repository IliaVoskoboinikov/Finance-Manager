package soft.divan.financemanager.feature.security.impl.presenter.viewmodel

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import soft.divan.financemanager.core.auth.domain.model.AuthStatus
import soft.divan.financemanager.core.auth.domain.usecase.GetAuthStatusUseCase
import soft.divan.financemanager.core.data.vault.BiometricEnrollmentStart
import soft.divan.financemanager.core.data.vault.BiometricSession
import soft.divan.financemanager.core.data.vault.LocalDataBiometrics
import soft.divan.financemanager.core.domain.model.ProtectionChangeResult
import soft.divan.financemanager.feature.security.impl.domain.model.DeletePinResult
import soft.divan.financemanager.feature.security.impl.domain.usecase.ChangePinUseCase
import soft.divan.financemanager.feature.security.impl.domain.usecase.DeletePinUseCase
import soft.divan.financemanager.feature.security.impl.domain.usecase.RecoverLocalDataUseCase
import soft.divan.financemanager.feature.security.impl.presenter.model.PinPrompt
import soft.divan.financemanager.feature.security.impl.presenter.model.PinPurpose
import soft.divan.financemanager.feature.security.impl.presenter.model.SecurityEvent
import soft.divan.financemanager.feature.security.impl.presenter.model.SecurityMessage
import javax.crypto.Cipher

/** Вспомогательные сценарии экрана безопасности: смена PIN, отпечаток, восстановление. */
@OptIn(ExperimentalCoroutinesApi::class)
class SettingsFlowsViewModelTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /* ---------- PinSettingsViewModel ---------- */

    private val changePin = mockk<ChangePinUseCase>()
    private val deletePin = mockk<DeletePinUseCase>()

    @Test
    fun `pin change asks for the current pin, then a new one`() {
        coEvery { changePin(PIN, NEW_PIN) } returns ProtectionChangeResult.Changed
        val vm = PinSettingsViewModel(changePin, deletePin)

        vm.onChangePinClicked()
        assertThat(vm.state.value.pinPrompt).isEqualTo(PinPrompt(PinPurpose.ChangePinCurrent))
        vm.onPinEntered(PIN)
        val next = vm.state.value.pinPrompt!!
        assertThat(next.create).isTrue()
        assertThat((next.purpose as PinPurpose.ChangePinNew).currentPin).isEqualTo(PIN)
        vm.onPinEntered(NEW_PIN)

        assertThat(vm.state.value.message).isEqualTo(SecurityMessage.PIN_CHANGED)
        assertThat(vm.state.value.inProgress).isFalse()
    }

    @Test
    fun `pin change failures are explained`() {
        coEvery { changePin(any(), any()) } returnsMany listOf(
            ProtectionChangeResult.WrongPin,
            ProtectionChangeResult.Failed,
            ProtectionChangeResult.UnsentChanges
        )
        val vm = PinSettingsViewModel(changePin, deletePin)

        listOf(SecurityMessage.WRONG_PIN, SecurityMessage.CHANGE_FAILED, SecurityMessage.CHANGE_FAILED)
            .forEach { expected ->
                vm.onChangePinClicked()
                vm.onPinEntered(PIN)
                vm.onPinEntered(NEW_PIN)
                assertThat(vm.state.value.message).isEqualTo(expected)
                vm.onMessageShown()
            }
        assertThat(vm.state.value.message).isNull()
    }

    @Test
    fun `pin deletion is refused at the pin level without asking`() {
        val vm = PinSettingsViewModel(changePin, deletePin)

        vm.onDeletePinClicked(dataProtectedByPin = true)

        assertThat(vm.state.value.pinPrompt).isNull()
        assertThat(vm.state.value.message).isEqualTo(SecurityMessage.DELETE_NOT_ALLOWED)
    }

    @Test
    fun `pin deletion reports every outcome`() {
        coEvery { deletePin(any()) } returnsMany listOf(
            DeletePinResult.DELETED,
            DeletePinResult.WRONG_PIN,
            DeletePinResult.NOT_ALLOWED
        )
        val vm = PinSettingsViewModel(changePin, deletePin)

        listOf(
            SecurityMessage.PIN_DELETED,
            SecurityMessage.WRONG_PIN,
            SecurityMessage.DELETE_NOT_ALLOWED
        )
            .forEach { expected ->
                vm.onDeletePinClicked(dataProtectedByPin = false)
                assertThat(vm.state.value.pinPrompt).isEqualTo(PinPrompt(PinPurpose.DeletePin))
                vm.onPinEntered(PIN)
                assertThat(vm.state.value.message).isEqualTo(expected)
            }
    }

    @Test
    fun `dismissed prompt drops the pin`() {
        val vm = PinSettingsViewModel(changePin, deletePin)
        vm.onChangePinClicked()

        vm.onDismissed()
        vm.onPinEntered(PIN)

        assertThat(vm.state.value.pinPrompt).isNull()
        coVerify(exactly = 0) { changePin(any(), any()) }
    }

    /* ---------- BiometricSettingsViewModel ---------- */

    private val biometrics = mockk<LocalDataBiometrics>(relaxUnitFun = true)

    @Test
    fun `enabling the fingerprint asks for the pin and then the system prompt`() = runTest {
        val cipher = mockk<Cipher>()
        val session = mockk<BiometricSession> { every { this@mockk.cipher } returns cipher }
        coEvery { biometrics.startEnrollment(PIN) } returns BiometricEnrollmentStart.Ready(session)
        coEvery { biometrics.finishEnrollment(session) } returns true
        val vm = BiometricSettingsViewModel(biometrics)

        vm.onToggled(true)
        assertThat(vm.state.value.pinPrompt).isEqualTo(PinPrompt(PinPurpose.EnableBiometric))
        vm.onPinEntered(PIN)

        val prompt = vm.events.first() as SecurityEvent.ShowBiometricPrompt
        assertThat(prompt.cipher).isSameAs(cipher)
        vm.onPromptSucceeded()

        assertThat(vm.state.value.message).isEqualTo(SecurityMessage.BIOMETRIC_ENABLED)
        assertThat(vm.state.value.inProgress).isFalse()
    }

    @Test
    fun `failed sealing is reported`() = runTest {
        val session = mockk<BiometricSession> { every { cipher } returns mockk() }
        coEvery { biometrics.startEnrollment(PIN) } returns BiometricEnrollmentStart.Ready(session)
        coEvery { biometrics.finishEnrollment(session) } returns false
        val vm = BiometricSettingsViewModel(biometrics)

        vm.enroll(PIN)
        vm.events.first()
        vm.onPromptSucceeded()

        assertThat(vm.state.value.message).isEqualTo(SecurityMessage.BIOMETRIC_FAILED)
    }

    @Test
    fun `wrong pin or missing hardware stop the enrollment`() {
        coEvery { biometrics.startEnrollment(any()) } returnsMany listOf(
            BiometricEnrollmentStart.WrongPin,
            BiometricEnrollmentStart.Unavailable
        )
        val vm = BiometricSettingsViewModel(biometrics)

        vm.enroll(PIN)
        assertThat(vm.state.value.message).isEqualTo(SecurityMessage.WRONG_PIN)
        vm.enroll(PIN)
        assertThat(vm.state.value.message).isEqualTo(SecurityMessage.BIOMETRIC_FAILED)
    }

    @Test
    fun `cancelled system prompt releases the session once`() = runTest {
        val session = mockk<BiometricSession> { every { cipher } returns mockk() }
        coEvery { biometrics.startEnrollment(PIN) } returns BiometricEnrollmentStart.Ready(session)
        val vm = BiometricSettingsViewModel(biometrics)
        vm.enroll(PIN)
        vm.events.first()

        vm.onPromptFailed()
        vm.onPromptFailed()
        vm.onPromptSucceeded()

        coVerify(exactly = 1) { biometrics.cancel(session) }
        coVerify(exactly = 0) { biometrics.finishEnrollment(any()) }
    }

    @Test
    fun `switching the fingerprint off removes the copy`() {
        val vm = BiometricSettingsViewModel(biometrics)

        vm.onToggled(false)
        vm.onDismissed()
        vm.onPinEntered(PIN)
        vm.onMessageShown()

        coVerify { biometrics.disable() }
        coVerify(exactly = 0) { biometrics.startEnrollment(any()) }
    }

    /* ---------- KeyLostViewModel ---------- */

    private val recover = mockk<RecoverLocalDataUseCase>(relaxUnitFun = true)
    private val authStatus =
        mockk<GetAuthStatusUseCase> { every { this@mockk.invoke() } returns flowOf(AuthStatus.GUEST) }

    @Test
    fun `key loss screen knows a guest cannot get the data back`() {
        val vm = KeyLostViewModel(recover, authStatus)

        assertThat(vm.uiState.value.isGuest).isTrue()
    }

    @Test
    fun `retry and confirmed wipe go to recovery`() {
        val vm = KeyLostViewModel(recover, authStatus)

        vm.onRetry()
        vm.onWipeClicked()
        assertThat(vm.uiState.value.showConfirm).isTrue()
        vm.onWipeDismissed()
        assertThat(vm.uiState.value.showConfirm).isFalse()
        vm.onWipeClicked()
        vm.onWipeConfirmed()

        coVerify(exactly = 1) { recover.retry() }
        coVerify(exactly = 1) { recover.recover() }
        assertThat(vm.uiState.value.inProgress).isFalse()
    }

    private companion object {
        const val PIN = "2468"
        const val NEW_PIN = "8642"
    }
}
