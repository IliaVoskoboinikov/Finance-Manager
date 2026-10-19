package soft.divan.financemanager.feature.security.impl.domain.usecase.impl

import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import soft.divan.financemanager.core.domain.model.DataProtectionLevel
import soft.divan.financemanager.core.domain.model.PinLockout
import soft.divan.financemanager.core.domain.model.PinUnlockResult
import soft.divan.financemanager.core.domain.model.ProtectionChangeResult
import soft.divan.financemanager.core.domain.repository.DataProtectionRepository
import soft.divan.financemanager.core.domain.repository.LocalDataAccessRepository
import soft.divan.financemanager.feature.security.impl.domain.model.DeletePinResult
import soft.divan.financemanager.feature.security.impl.domain.model.PinCheckResult
import soft.divan.financemanager.feature.security.impl.domain.model.PinLockStatus
import soft.divan.financemanager.feature.security.impl.domain.repository.SecurityRepository
import java.time.Instant

class PinUseCasesTest {

    private val localData = mockk<LocalDataAccessRepository>(relaxUnitFun = true)
    private val protection = mockk<DataProtectionRepository>()
    private val securityRepository = mockk<SecurityRepository>(relaxUnitFun = true)
    private val io = UnconfinedTestDispatcher()

    private val unlock = UnlockWithPinUseCaseImpl(localData, securityRepository, io)

    @Test
    fun `pin-level success opens the data`() = runTest {
        coEvery { localData.unlockWithPin(PIN) } returns PinUnlockResult.Success

        assertThat(unlock(PIN)).isEqualTo(PinCheckResult.Correct)
        verify { securityRepository wasNot Called }
    }

    @Test
    fun `below the pin level the hash is the lock`() = runTest {
        coEvery { localData.unlockWithPin(any()) } returns PinUnlockResult.NotRequired
        every { securityRepository.verifyPin(PIN) } returns true
        every { securityRepository.verifyPin(WRONG) } returns false

        assertThat(unlock(PIN)).isEqualTo(PinCheckResult.Correct)
        assertThat(unlock(WRONG)).isEqualTo(PinCheckResult.Wrong(PinLockStatus.UNLIMITED))
    }

    @Test
    fun `pin-level failures carry the lockout`() = runTest {
        val until = Instant.parse("2026-09-01T12:00:30Z")
        coEvery { localData.unlockWithPin(WRONG) } returns
            PinUnlockResult.WrongPin(PinLockout(attemptsLeft = 5, lockedUntil = until))
        coEvery { localData.unlockWithPin(PIN) } returns
            PinUnlockResult.LockedOut(PinLockout(attemptsLeft = 5, lockedUntil = until))

        assertThat(unlock(WRONG)).isEqualTo(PinCheckResult.Wrong(PinLockStatus(5, until)))
        assertThat(unlock(PIN)).isEqualTo(PinCheckResult.LockedOut(PinLockStatus(5, until)))
    }

    @Test
    fun `wiped data takes the pin with it`() = runTest {
        coEvery { localData.unlockWithPin(any()) } returns PinUnlockResult.Wiped

        assertThat(unlock(WRONG)).isEqualTo(PinCheckResult.Wiped)
        verify { securityRepository.deletePin() }
    }

    @Test
    fun `lost key is reported`() = runTest {
        coEvery { localData.unlockWithPin(any()) } returns PinUnlockResult.KeyLost

        assertThat(unlock(PIN)).isEqualTo(PinCheckResult.KeyLost)
    }

    @Test
    fun `lock status is counted only at the pin level`() = runTest {
        val status = GetPinLockStatusUseCaseImpl(protection, localData)
        coEvery { localData.pinLockout() } returns PinLockout(attemptsLeft = 4)

        coEvery { protection.currentLevel() } returns DataProtectionLevel.PIN
        assertThat(status()).isEqualTo(PinLockStatus(attemptsLeft = 4))

        coEvery { protection.currentLevel() } returns DataProtectionLevel.DEVICE
        assertThat(status()).isEqualTo(PinLockStatus.UNLIMITED)
    }

    @Test
    fun `forgotten pin wipes the data first, then removes the pin`() = runTest {
        ForgetPinUseCaseImpl(localData, securityRepository, io)()

        coVerifyOrder {
            localData.wipe(signOut = true)
            securityRepository.deletePin()
        }
    }

    @Test
    fun `recovery retry re-reads the keys`() = runTest {
        val recover = RecoverLocalDataUseCaseImpl(localData, securityRepository, io)

        recover.retry()

        coVerify { localData.initialize() }
    }

    @Test
    fun `recovery with sign-out removes the pin after the data`() = runTest {
        val recover = RecoverLocalDataUseCaseImpl(localData, securityRepository, io)
        coEvery { localData.recover() } returns true

        recover.recover()

        coVerifyOrder {
            localData.recover()
            securityRepository.deletePin()
        }
    }

    @Test
    fun `recovery that keeps the session keeps the pin as the app lock`() = runTest {
        val recover = RecoverLocalDataUseCaseImpl(localData, securityRepository, io)
        coEvery { localData.recover() } returns false

        recover.recover()

        coVerify { localData.recover() }
        verify(exactly = 0) { securityRepository.deletePin() }
    }

    @Test
    fun `pin change below the pin level checks the hash first`() = runTest {
        val change = ChangePinUseCaseImpl(protection, securityRepository, io)
        coEvery { protection.currentLevel() } returns DataProtectionLevel.DEVICE
        every { securityRepository.verifyPin(WRONG) } returns false

        assertThat(change(WRONG, NEW_PIN)).isEqualTo(ProtectionChangeResult.WrongPin)
        coVerify(exactly = 0) { protection.changePin(any(), any()) }
    }

    @Test
    fun `pin change saves the new hash once the key is rewrapped`() = runTest {
        val change = ChangePinUseCaseImpl(protection, securityRepository, io)
        coEvery { protection.currentLevel() } returns DataProtectionLevel.PIN
        coEvery { protection.changePin(PIN, NEW_PIN) } returns ProtectionChangeResult.Changed

        assertThat(change(PIN, NEW_PIN)).isEqualTo(ProtectionChangeResult.Changed)

        // На уровне PIN текущий PIN проверяет хранилище ключей, а не хеш
        verify(exactly = 0) { securityRepository.verifyPin(any()) }
        verify { securityRepository.savePin(NEW_PIN) }
    }

    @Test
    fun `failed pin change keeps the old hash`() = runTest {
        val change = ChangePinUseCaseImpl(protection, securityRepository, io)
        coEvery { protection.currentLevel() } returns DataProtectionLevel.PIN
        coEvery { protection.changePin(any(), any()) } returns ProtectionChangeResult.WrongPin

        assertThat(change(WRONG, NEW_PIN)).isEqualTo(ProtectionChangeResult.WrongPin)
        verify(exactly = 0) { securityRepository.savePin(any()) }
    }

    @Test
    fun `pin cannot be deleted while it wraps the data key`() = runTest {
        val delete = DeletePinUseCaseImpl(protection, securityRepository, io)
        coEvery { protection.currentLevel() } returns DataProtectionLevel.PIN

        assertThat(delete(PIN)).isEqualTo(DeletePinResult.NOT_ALLOWED)
        verify(exactly = 0) { securityRepository.deletePin() }
    }

    @Test
    fun `pin deletion needs the current pin`() = runTest {
        val delete = DeletePinUseCaseImpl(protection, securityRepository, io)
        coEvery { protection.currentLevel() } returns DataProtectionLevel.NONE
        every { securityRepository.verifyPin(WRONG) } returns false
        every { securityRepository.verifyPin(PIN) } returns true

        assertThat(delete(WRONG)).isEqualTo(DeletePinResult.WRONG_PIN)
        assertThat(delete(PIN)).isEqualTo(DeletePinResult.DELETED)
        verify(exactly = 1) { securityRepository.deletePin() }
    }

    @Test
    fun `last attempt is flagged`() {
        assertThat(PinLockStatus(attemptsLeft = 1).isLastAttempt).isTrue()
        assertThat(PinLockStatus(attemptsLeft = 2).isLastAttempt).isFalse()
        assertThat(PinLockStatus.UNLIMITED.isLastAttempt).isFalse()
    }

    private companion object {
        const val PIN = "2468"
        const val NEW_PIN = "8642"
        const val WRONG = "1357"
    }
}
