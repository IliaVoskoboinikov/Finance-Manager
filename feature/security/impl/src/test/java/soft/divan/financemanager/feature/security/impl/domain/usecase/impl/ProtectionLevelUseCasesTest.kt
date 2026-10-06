package soft.divan.financemanager.feature.security.impl.domain.usecase.impl

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import soft.divan.financemanager.core.auth.domain.model.AuthStatus
import soft.divan.financemanager.core.auth.domain.usecase.GetAuthStatusUseCase
import soft.divan.financemanager.core.data.vault.LocalDataBiometrics
import soft.divan.financemanager.core.domain.model.DataProtectionLevel
import soft.divan.financemanager.core.domain.model.ProtectionChangeResult
import soft.divan.financemanager.core.domain.repository.DataProtectionRepository
import soft.divan.financemanager.feature.security.impl.domain.model.SecuritySettings
import soft.divan.financemanager.feature.security.impl.domain.repository.SecureScreenRepository
import soft.divan.financemanager.feature.security.impl.domain.repository.SecurityRepository

class ProtectionLevelUseCasesTest {

    private val protection = mockk<DataProtectionRepository>()
    private val securityRepository = mockk<SecurityRepository>(relaxUnitFun = true)
    private val io = UnconfinedTestDispatcher()
    private val change = ChangeProtectionLevelUseCaseImpl(protection, securityRepository, io)

    @Test
    fun `same level is a no-op`() = runTest {
        coEvery { protection.currentLevel() } returns DataProtectionLevel.DEVICE

        assertThat(
            change(DataProtectionLevel.DEVICE, pin = null)
        ).isEqualTo(ProtectionChangeResult.Changed)
        coVerify(exactly = 0) { protection.changeLevel(any(), any()) }
    }

    @Test
    fun `pin level is entered only with the app pin`() = runTest {
        coEvery { protection.currentLevel() } returns DataProtectionLevel.DEVICE
        every { securityRepository.verifyPin(WRONG) } returns false

        assertThat(change(DataProtectionLevel.PIN, WRONG)).isEqualTo(ProtectionChangeResult.WrongPin)
        assertThat(
            change(DataProtectionLevel.PIN, pin = null)
        ).isEqualTo(ProtectionChangeResult.WrongPin)
        coVerify(exactly = 0) { protection.changeLevel(any(), any()) }
    }

    @Test
    fun `pin level wraps the key with the app pin`() = runTest {
        coEvery { protection.currentLevel() } returns DataProtectionLevel.DEVICE
        every { securityRepository.verifyPin(PIN) } returns true
        coEvery {
            protection.changeLevel(
                DataProtectionLevel.PIN,
                PIN
            )
        } returns ProtectionChangeResult.Changed

        assertThat(change(DataProtectionLevel.PIN, PIN)).isEqualTo(ProtectionChangeResult.Changed)
        verify(exactly = 0) { securityRepository.savePin(any()) }
    }

    @Test
    fun `new pin is saved together with the pin level`() = runTest {
        coEvery { protection.currentLevel() } returns DataProtectionLevel.DEVICE
        every { securityRepository.verifyPin(PIN) } returns true
        coEvery {
            protection.changeLevel(
                DataProtectionLevel.PIN,
                PIN
            )
        } returns ProtectionChangeResult.Changed

        assertThat(
            change(DataProtectionLevel.PIN, PIN, isNewPin = true)
        ).isEqualTo(ProtectionChangeResult.Changed)

        verifyOrder {
            securityRepository.savePin(PIN)
            securityRepository.verifyPin(PIN)
        }
        verify(exactly = 0) { securityRepository.deletePin() }
    }

    @Test
    fun `new pin does not outlive a refused pin level`() = runTest {
        coEvery { protection.currentLevel() } returns DataProtectionLevel.DEVICE
        every { securityRepository.verifyPin(PIN) } returns true
        coEvery {
            protection.changeLevel(DataProtectionLevel.PIN, PIN)
        } returns ProtectionChangeResult.UnsentChanges

        assertThat(change(DataProtectionLevel.PIN, PIN, isNewPin = true))
            .isEqualTo(ProtectionChangeResult.UnsentChanges)
        verify { securityRepository.deletePin() }
    }

    @Test
    fun `leaving the pin level resyncs the hash with the pin that opened the key`() = runTest {
        coEvery { protection.currentLevel() } returns DataProtectionLevel.PIN
        coEvery {
            protection.changeLevel(DataProtectionLevel.DEVICE, PIN)
        } returns ProtectionChangeResult.Changed

        assertThat(change(DataProtectionLevel.DEVICE, PIN)).isEqualTo(ProtectionChangeResult.Changed)
        verify { securityRepository.savePin(PIN) }
        // Проверку PIN вело хранилище ключей, а не хеш
        verify(exactly = 0) { securityRepository.verifyPin(any()) }
    }

    @Test
    fun `failed step-down leaves the hash alone`() = runTest {
        coEvery { protection.currentLevel() } returns DataProtectionLevel.PIN
        coEvery { protection.changeLevel(any(), any()) } returns ProtectionChangeResult.WrongPin

        assertThat(change(DataProtectionLevel.NONE, WRONG)).isEqualTo(ProtectionChangeResult.WrongPin)
        verify(exactly = 0) { securityRepository.savePin(any()) }
    }

    @Test
    fun `lowering without a pin level needs no pin`() = runTest {
        coEvery { protection.currentLevel() } returns DataProtectionLevel.DEVICE
        coEvery {
            protection.changeLevel(
                DataProtectionLevel.NONE,
                null
            )
        } returns ProtectionChangeResult.Changed

        assertThat(
            change(DataProtectionLevel.NONE, pin = null)
        ).isEqualTo(ProtectionChangeResult.Changed)
    }

    @Test
    fun `level is observed from the repository`() = runTest {
        every { protection.observeLevel() } returns flowOf(DataProtectionLevel.PIN)

        assertThat(
            ObserveProtectionLevelUseCaseImpl(protection)().first()
        ).isEqualTo(DataProtectionLevel.PIN)
    }

    @Test
    fun `settings combine every source`() = runTest {
        every { protection.observeLevel() } returns flowOf(DataProtectionLevel.PIN)
        every { securityRepository.observePinSet() } returns flowOf(true)
        val biometrics = mockk<LocalDataBiometrics> { every { observeEnabled() } returns flowOf(true) }
        val secureScreen =
            mockk<SecureScreenRepository> { every { observeEnabled() } returns flowOf(false) }
        val authStatus =
            mockk<GetAuthStatusUseCase> {
                every { this@mockk.invoke() } returns flowOf(
                    AuthStatus.GUEST
                )
            }

        val settings = ObserveSecuritySettingsUseCaseImpl(
            protection,
            securityRepository,
            biometrics,
            secureScreen,
            authStatus
        )().first()

        assertThat(settings).isEqualTo(
            SecuritySettings(
                level = DataProtectionLevel.PIN,
                hasPin = true,
                biometricEnabled = true,
                secureScreen = false,
                isGuest = true
            )
        )
    }

    @Test
    fun `secure screen use cases go to the repository`() = runTest {
        val repository = mockk<SecureScreenRepository>(relaxUnitFun = true) {
            every { observeEnabled() } returns flowOf(true)
        }

        assertThat(ObserveSecureScreenUseCaseImpl(repository)().first()).isTrue()
        SetSecureScreenUseCaseImpl(repository)(false)
        coVerify { repository.setEnabled(false) }
    }

    private companion object {
        const val PIN = "2468"
        const val WRONG = "1357"
    }
}
