package soft.divan.financemanager.feature.security.impl.presenter.viewmodel

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
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
import soft.divan.financemanager.core.data.vault.BiometricSession
import soft.divan.financemanager.core.data.vault.LocalDataBiometrics
import soft.divan.financemanager.core.domain.model.DataProtectionLevel
import soft.divan.financemanager.feature.security.impl.domain.model.PinCheckResult
import soft.divan.financemanager.feature.security.impl.domain.model.PinLockStatus
import soft.divan.financemanager.feature.security.impl.domain.usecase.ForgetPinUseCase
import soft.divan.financemanager.feature.security.impl.domain.usecase.GetPinLockStatusUseCase
import soft.divan.financemanager.feature.security.impl.domain.usecase.ObserveProtectionLevelUseCase
import soft.divan.financemanager.feature.security.impl.domain.usecase.UnlockWithPinUseCase
import soft.divan.financemanager.feature.security.impl.presenter.model.PinLockError
import soft.divan.financemanager.feature.security.impl.presenter.model.PinLockEvent
import soft.divan.financemanager.feature.security.impl.presenter.model.PinLockMessage
import soft.divan.financemanager.feature.security.impl.presenter.model.message
import java.time.Instant
import javax.crypto.Cipher

@OptIn(ExperimentalCoroutinesApi::class)
class PinLockViewModelTest {

    private val unlockWithPin = mockk<UnlockWithPinUseCase>()
    private val getPinLockStatus = mockk<GetPinLockStatusUseCase>()
    private val forgetPin = mockk<ForgetPinUseCase>(relaxUnitFun = true)
    private val observeLevel = mockk<ObserveProtectionLevelUseCase>()
    private val biometrics = mockk<LocalDataBiometrics>(relaxUnitFun = true)
    private val authStatus = mockk<GetAuthStatusUseCase>()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        coEvery { getPinLockStatus() } returns PinLockStatus(attemptsLeft = 10)
        every { authStatus() } returns flowOf(AuthStatus.AUTHORIZED)
        every { biometrics.observeEnabled() } returns flowOf(true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(level: DataProtectionLevel = DataProtectionLevel.PIN): PinLockViewModel {
        every { observeLevel() } returns flowOf(level)
        return PinLockViewModel(
            unlockWithPin,
            getPinLockStatus,
            forgetPin,
            observeLevel,
            biometrics,
            authStatus
        )
            .also { it.onShown() }
    }

    @Test
    fun `showing the lock reads the level, lockout and session`() {
        val vm = viewModel()

        val state = vm.uiState.value
        assertThat(state.dataProtectedByPin).isTrue()
        assertThat(state.status).isEqualTo(PinLockStatus(attemptsLeft = 10))
        assertThat(state.biometricEnabled).isTrue()
        assertThat(state.isGuest).isFalse()
    }

    @Test
    fun `below the pin level biometrics only unlock the interface`() = runTest {
        every { biometrics.observeEnabled() } returns flowOf(false)
        val vm = viewModel(DataProtectionLevel.DEVICE)

        assertThat(vm.uiState.value.biometricEnabled).isTrue()

        vm.onBiometricRequested()
        val event = vm.events.first() as PinLockEvent.ShowBiometricPrompt
        assertThat(event.cipher).isNull()

        vm.onBiometricSucceeded()
        assertThat(vm.events.first()).isEqualTo(PinLockEvent.Unlocked)
    }

    @Test
    fun `right pin unlocks`() = runTest {
        coEvery { unlockWithPin(PIN) } returns PinCheckResult.Correct
        val vm = viewModel()

        vm.onPinEntered(PIN)

        assertThat(vm.events.first()).isEqualTo(PinLockEvent.Unlocked)
        assertThat(vm.uiState.value.inProgress).isFalse()
    }

    @Test
    fun `wrong pin shows how many attempts are left`() {
        coEvery { unlockWithPin(any()) } returns PinCheckResult.Wrong(PinLockStatus(attemptsLeft = 3))
        val vm = viewModel()

        vm.onPinEntered(PIN)

        assertThat(vm.uiState.value.error).isEqualTo(PinLockError.WrongPin(3))
        assertThat(vm.uiState.value.status.attemptsLeft).isEqualTo(3)
    }

    @Test
    fun `lockout blocks the input until it expires`() {
        val until = Instant.parse("2026-09-01T12:01:00Z")
        coEvery { unlockWithPin(any()) } returns PinCheckResult.LockedOut(PinLockStatus(5, until))
        val vm = viewModel()

        vm.onPinEntered(PIN)
        assertThat(vm.uiState.value.error).isEqualTo(PinLockError.LockedOut)
        assertThat(vm.uiState.value.status.lockedUntil).isEqualTo(until)

        vm.onLockoutExpired()
        assertThat(vm.uiState.value.status.lockedUntil).isNull()
        assertThat(vm.uiState.value.error).isNull()
    }

    @Test
    fun `after the pause of the ninth failure the last attempt is announced`() {
        val until = Instant.parse("2026-09-01T13:00:00Z")
        coEvery { unlockWithPin(any()) } returns PinCheckResult.Wrong(PinLockStatus(1, until))
        val vm = viewModel()

        vm.onPinEntered(PIN)
        assertThat(vm.uiState.value.message(lockoutSecondsLeft = 3_600))
            .isEqualTo(PinLockMessage.LockedOut(3_600))

        vm.onLockoutExpired()
        assertThat(vm.uiState.value.message(lockoutSecondsLeft = 0))
            .isEqualTo(PinLockMessage.LastAttempt)
    }

    @Test
    fun `reopened lock with one attempt left announces it`() {
        coEvery { getPinLockStatus() } returns PinLockStatus(attemptsLeft = 1)

        val vm = viewModel()

        assertThat(vm.uiState.value.message(lockoutSecondsLeft = 0))
            .isEqualTo(PinLockMessage.LastAttempt)
    }

    @Test
    fun `exhausted attempts lead out of the lock`() = runTest {
        coEvery { unlockWithPin(any()) } returns PinCheckResult.Wiped
        val vm = viewModel()

        vm.onPinEntered(PIN)

        assertThat(vm.events.first()).isEqualTo(PinLockEvent.Wiped)
    }

    @Test
    fun `lost key leaves the recovery to the host`() {
        coEvery { unlockWithPin(any()) } returns PinCheckResult.KeyLost
        val vm = viewModel()

        vm.onPinEntered(PIN)

        assertThat(vm.uiState.value.error).isNull()
        assertThat(vm.uiState.value.inProgress).isFalse()
    }

    @Test
    fun `biometric unlock goes through the vault session`() = runTest {
        val cipher = mockk<Cipher>()
        val session = mockk<BiometricSession> { every { this@mockk.cipher } returns cipher }
        coEvery { biometrics.startUnlock() } returns session
        coEvery { biometrics.finishUnlock(session) } returns true
        val vm = viewModel()

        vm.onBiometricRequested()
        val prompt = vm.events.first() as PinLockEvent.ShowBiometricPrompt
        assertThat(prompt.cipher).isSameAs(cipher)

        vm.onBiometricSucceeded()
        assertThat(vm.events.first()).isEqualTo(PinLockEvent.Unlocked)
    }

    @Test
    fun `invalidated biometric copy falls back to the pin`() {
        coEvery { biometrics.startUnlock() } returns null
        val vm = viewModel()

        vm.onBiometricRequested()

        assertThat(vm.uiState.value.biometricEnabled).isFalse()
        assertThat(vm.uiState.value.error).isEqualTo(PinLockError.BiometricInvalidated)
    }

    @Test
    fun `failed biometric finish is reported`() = runTest {
        val session = mockk<BiometricSession> { every { cipher } returns mockk() }
        coEvery { biometrics.startUnlock() } returns session
        coEvery { biometrics.finishUnlock(session) } returns false
        val vm = viewModel()
        vm.onBiometricRequested()
        vm.events.first()

        vm.onBiometricSucceeded()

        assertThat(vm.uiState.value.error).isEqualTo(PinLockError.BiometricInvalidated)
    }

    @Test
    fun `cancelled biometric prompt releases the session`() = runTest {
        val session = mockk<BiometricSession> { every { cipher } returns mockk() }
        coEvery { biometrics.startUnlock() } returns session
        val vm = viewModel()
        vm.onBiometricRequested()
        vm.events.first()

        vm.onBiometricFailed()
        vm.onBiometricFailed()

        coVerify(exactly = 1) { biometrics.cancel(session) }
    }

    @Test
    fun `stale session from a previous showing is released`() = runTest {
        val session = mockk<BiometricSession> { every { cipher } returns mockk() }
        coEvery { biometrics.startUnlock() } returns session
        val vm = viewModel()
        vm.onBiometricRequested()
        vm.events.first()

        vm.onShown()

        coVerify { biometrics.cancel(session) }
    }

    @Test
    fun `forgotten pin is wiped only after confirmation`() = runTest {
        every { authStatus() } returns flowOf(AuthStatus.GUEST)
        val vm = viewModel()
        assertThat(vm.uiState.value.isGuest).isTrue()

        vm.onForgotPinClicked()
        assertThat(vm.uiState.value.showForgotPinDialog).isTrue()
        vm.onForgotPinDismissed()
        assertThat(vm.uiState.value.showForgotPinDialog).isFalse()
        coVerify(exactly = 0) { forgetPin() }

        vm.onForgotPinClicked()
        vm.onForgotPinConfirmed()

        coVerify(exactly = 1) { forgetPin() }
        assertThat(vm.events.first()).isEqualTo(PinLockEvent.Wiped)
    }

    @Test
    fun `input is ignored while a wipe is running`() = runTest {
        val gate = CompletableDeferred<Unit>()
        coEvery { forgetPin() } coAnswers { gate.await() }
        val vm = viewModel()
        vm.onForgotPinClicked()
        vm.onForgotPinConfirmed()
        assertThat(vm.uiState.value.inProgress).isTrue()

        vm.onBiometricRequested()
        vm.onPinEntered(PIN)

        coVerify(exactly = 0) { biometrics.startUnlock() }
        coVerify(exactly = 0) { unlockWithPin(any()) }
        gate.complete(Unit)
    }

    private companion object {
        const val PIN = "2468"
    }
}
