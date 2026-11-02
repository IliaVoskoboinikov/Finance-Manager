package soft.divan.financemanager.feature.security.impl.presenter.viewmodel

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import soft.divan.financemanager.core.domain.model.DataProtectionLevel
import soft.divan.financemanager.core.domain.model.ProtectionChangeResult
import soft.divan.financemanager.feature.security.impl.domain.model.SecuritySettings
import soft.divan.financemanager.feature.security.impl.domain.usecase.ChangeProtectionLevelUseCase
import soft.divan.financemanager.feature.security.impl.domain.usecase.ObserveSecuritySettingsUseCase
import soft.divan.financemanager.feature.security.impl.domain.usecase.SetSecureScreenUseCase
import soft.divan.financemanager.feature.security.impl.presenter.model.PinPrompt
import soft.divan.financemanager.feature.security.impl.presenter.model.PinPurpose
import soft.divan.financemanager.feature.security.impl.presenter.model.SecurityDialog
import soft.divan.financemanager.feature.security.impl.presenter.model.SecurityEvent
import soft.divan.financemanager.feature.security.impl.presenter.model.SecurityMessage
import soft.divan.financemanager.feature.security.impl.presenter.model.SecurityUiState

@OptIn(ExperimentalCoroutinesApi::class)
class SecurityViewModelTest {

    private val settings = MutableStateFlow(settings(level = DataProtectionLevel.DEVICE))
    private val observeSettings = mockk<ObserveSecuritySettingsUseCase> {
        every { this@mockk.invoke() } returns this@SecurityViewModelTest.settings
    }
    private val changeLevel = mockk<ChangeProtectionLevelUseCase>()
    private val setSecureScreen = mockk<SetSecureScreenUseCase>(relaxUnitFun = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `settings are shown as they are`() = vmTest { vm ->
        assertThat(vm.success().settings).isEqualTo(settings.value)
    }

    @Test
    fun `raising none to device needs no confirmation`() = vmTest { vm ->
        settings.value = settings(level = DataProtectionLevel.NONE)
        coEvery {
            changeLevel(
                DataProtectionLevel.DEVICE,
                null,
                false
            )
        } returns ProtectionChangeResult.Changed

        vm.onLevelSelected(DataProtectionLevel.DEVICE)

        assertThat(vm.success().dialog).isNull()
        assertThat(vm.success().message).isEqualTo(SecurityMessage.LEVEL_CHANGED)
    }

    @Test
    fun `selecting the current level does nothing`() = vmTest { vm ->
        vm.onLevelSelected(DataProtectionLevel.DEVICE)

        assertThat(vm.success().dialog).isNull()
        coVerify(exactly = 0) { changeLevel(any(), any(), any()) }
    }

    @Test
    fun `lowering to none asks first and then applies without pin`() = vmTest { vm ->
        coEvery {
            changeLevel(
                DataProtectionLevel.NONE,
                null,
                false
            )
        } returns ProtectionChangeResult.Changed

        vm.onLevelSelected(DataProtectionLevel.NONE)
        assertThat(vm.success().dialog).isEqualTo(SecurityDialog.ConfirmLevel(DataProtectionLevel.NONE))

        vm.onLevelConfirmed(DataProtectionLevel.NONE)

        assertThat(vm.success().dialog).isNull()
        assertThat(vm.success().pinPrompt).isNull()
        assertThat(vm.success().message).isEqualTo(SecurityMessage.LEVEL_CHANGED)
    }

    @Test
    fun `pin level without a pin creates one`() = vmTest { vm ->
        settings.value = settings(level = DataProtectionLevel.DEVICE, hasPin = false)
        coEvery {
            changeLevel(
                DataProtectionLevel.PIN,
                PIN,
                true
            )
        } returns ProtectionChangeResult.Changed

        vm.onLevelSelected(DataProtectionLevel.PIN)
        vm.onLevelConfirmed(DataProtectionLevel.PIN)
        assertThat(vm.success().pinPrompt).isEqualTo(PinPrompt(PinPurpose.EnablePinLevel, create = true))

        vm.onPinEntered(PIN)

        coVerify { changeLevel(DataProtectionLevel.PIN, PIN, true) }
        assertThat(vm.success().message).isEqualTo(SecurityMessage.LEVEL_CHANGED)
    }

    @Test
    fun `pin level with biometrics offers the fingerprint with the same pin`() = vmTest { vm ->
        coEvery {
            changeLevel(
                DataProtectionLevel.PIN,
                PIN,
                false
            )
        } returns ProtectionChangeResult.Changed
        vm.onBiometricAvailability(true)

        vm.onLevelConfirmed(DataProtectionLevel.PIN)
        assertThat(
            vm.success().pinPrompt
        ).isEqualTo(PinPrompt(PinPurpose.EnablePinLevel, create = false))
        vm.onPinEntered(PIN)
        assertThat(vm.success().dialog).isEqualTo(SecurityDialog.OfferBiometric)

        vm.onBiometricOfferAccepted()

        val event = vm.events.first() as SecurityEvent.EnrollBiometric
        assertThat(event.pin).isEqualTo(PIN)
        assertThat(vm.success().dialog).isNull()
    }

    @Test
    fun `declined offer forgets the pin`() = vmTest { vm ->
        coEvery { changeLevel(any(), any(), any()) } returns ProtectionChangeResult.Changed
        vm.onBiometricAvailability(true)
        vm.onLevelConfirmed(DataProtectionLevel.PIN)
        vm.onPinEntered(PIN)

        vm.onDismissed()
        vm.onBiometricOfferAccepted()

        assertThat(vm.success().dialog).isNull()
    }

    @Test
    fun `leaving the pin level asks for the pin`() = vmTest { vm ->
        settings.value = settings(level = DataProtectionLevel.PIN)
        coEvery {
            changeLevel(
                DataProtectionLevel.DEVICE,
                PIN,
                false
            )
        } returns ProtectionChangeResult.Changed

        vm.onLevelSelected(DataProtectionLevel.DEVICE)
        vm.onLevelConfirmed(DataProtectionLevel.DEVICE)
        assertThat(vm.success().pinPrompt)
            .isEqualTo(PinPrompt(PinPurpose.LeavePinLevel(DataProtectionLevel.DEVICE)))

        vm.onPinEntered(PIN)

        assertThat(vm.success().message).isEqualTo(SecurityMessage.LEVEL_CHANGED)
    }

    @Test
    fun `refusals are explained`() = vmTest { vm ->
        coEvery { changeLevel(any(), any(), any()) } returnsMany listOf(
            ProtectionChangeResult.WrongPin,
            ProtectionChangeResult.UnsentChanges,
            ProtectionChangeResult.Failed
        )

        vm.onLevelConfirmed(DataProtectionLevel.PIN)
        vm.onPinEntered(PIN)
        assertThat(vm.success().message).isEqualTo(SecurityMessage.WRONG_PIN)
        vm.onMessageShown()

        vm.onLevelConfirmed(DataProtectionLevel.PIN)
        vm.onPinEntered(PIN)
        assertThat(vm.success().dialog).isEqualTo(SecurityDialog.UnsentChanges)
        vm.onDismissed()

        vm.onLevelConfirmed(DataProtectionLevel.PIN)
        vm.onPinEntered(PIN)
        assertThat(vm.success().message).isEqualTo(SecurityMessage.CHANGE_FAILED)
        assertThat(vm.success().inProgress).isFalse()
    }

    @Test
    fun `pin without a prompt is ignored`() = vmTest { vm ->
        vm.onPinEntered(PIN)

        coVerify(exactly = 0) { changeLevel(any(), any(), any()) }
    }

    @Test
    fun `secure screen toggle is saved`() = vmTest { vm ->
        vm.onSecureScreenToggled(false)

        coVerify { setSecureScreen(false) }
    }

    /** Состояние живёт, пока на него подписаны (`WhileSubscribed`), — подписываемся на весь тест. */
    private fun vmTest(body: suspend TestScope.(SecurityViewModel) -> Unit) = runTest {
        val vm = SecurityViewModel(observeSettings, changeLevel, setSecureScreen)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }
        body(vm)
    }

    private fun SecurityViewModel.success() = uiState.value as SecurityUiState.Success

    private fun settings(level: DataProtectionLevel, hasPin: Boolean = true) = SecuritySettings(
        level = level,
        hasPin = hasPin,
        biometricEnabled = false,
        secureScreen = true,
        isGuest = false
    )

    private companion object {
        const val PIN = "2468"
    }
}
