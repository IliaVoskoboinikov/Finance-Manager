package soft.divan.financemanager.core.data.repository

import android.util.Log
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import soft.divan.financemanager.core.auth.domain.model.AuthEvent
import soft.divan.financemanager.core.auth.domain.provider.AuthStateProvider
import soft.divan.financemanager.core.data.vault.LocalDataVault
import soft.divan.financemanager.core.data.vault.PinUnlocker
import soft.divan.financemanager.core.data.vault.VaultCore
import soft.divan.financemanager.core.data.vault.VaultPinResult
import soft.divan.financemanager.core.domain.model.LocalDataState
import soft.divan.financemanager.core.domain.model.PinLockout
import soft.divan.financemanager.core.domain.model.PinUnlockResult
import soft.divan.financemanager.core.security.keyset.KeyLevel
import soft.divan.financemanager.core.security.keyset.Keyset

class LocalDataAccessRepositoryImplTest {

    private val state = MutableStateFlow<LocalDataState>(LocalDataState.Open)
    private val vault = mockk<LocalDataVault>(relaxUnitFun = true) {
        every { state } returns this@LocalDataAccessRepositoryImplTest.state
    }
    private val unlocker = mockk<PinUnlocker>()
    private val core = mockk<VaultCore>()
    private val auth = mockk<AuthStateProvider>(relaxUnitFun = true)

    private val repository = LocalDataAccessRepositoryImpl(vault, unlocker, core, auth)

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.w(any(), any<String>()) } returns 0
    }

    @After
    fun tearDown() {
        unmockkStatic(Log::class)
    }

    @Test
    fun `state comes from the vault`() {
        assertThat(repository.state.value).isEqualTo(LocalDataState.Open)
    }

    @Test
    fun `initialize, lock and lockout go to the vault`() = runTest {
        coEvery { unlocker.pinLockout() } returns PinLockout(attemptsLeft = 7)

        repository.initialize()
        repository.lock()

        assertThat(repository.pinLockout()).isEqualTo(PinLockout(attemptsLeft = 7))
        coVerify { vault.initialize() }
        coVerify { vault.lock() }
    }

    @Test
    fun `pin check result is passed through`() = runTest {
        coEvery { unlocker.unlockWithPin(any()) } returns VaultPinResult.Done(PinUnlockResult.Success)

        assertThat(repository.unlockWithPin("1234")).isEqualTo(PinUnlockResult.Success)
        coVerify(exactly = 0) { auth.sendEvent(any()) }
        coVerify { unlocker.unlockWithPin(match { String(it) == "1234" }) }
    }

    @Test
    fun `exhausted attempts wipe the data and sign out`() = runTest {
        coEvery { unlocker.unlockWithPin(any()) } returns VaultPinResult.Exhausted

        val result = repository.unlockWithPin("1234")

        assertThat(result).isEqualTo(PinUnlockResult.Wiped)
        coVerify { auth.sendEvent(AuthEvent.OnSessionExpired) }
    }

    @Test
    fun `wipe with sign-out goes through the session so tokens are cleared too`() = runTest {
        repository.wipe(signOut = true)

        coVerify { auth.sendEvent(AuthEvent.OnSessionExpired) }
        coVerify(exactly = 0) { vault.wipe() }
    }

    @Test
    fun `wipe without sign-out keeps the session`() = runTest {
        repository.wipe(signOut = false)

        coVerify { vault.wipe() }
        coVerify(exactly = 0) { auth.sendEvent(any()) }
    }

    @Test
    fun `recovery at the device level keeps the session so data re-syncs`() = runTest {
        coEvery { core.readKeysetOrNull() } returns Keyset(level = KeyLevel.DEVICE)

        val signedOut = repository.recover()

        assertThat(signedOut).isFalse()
        coVerify { vault.wipe() }
        coVerify(exactly = 0) { auth.sendEvent(any()) }
    }

    @Test
    fun `recovery at the pin level signs out so the pin is not bypassed`() = runTest {
        coEvery { core.readKeysetOrNull() } returns Keyset(level = KeyLevel.PIN)

        val signedOut = repository.recover()

        assertThat(signedOut).isTrue()
        coVerify { auth.sendEvent(AuthEvent.OnSessionExpired) }
    }

    @Test
    fun `recovery with an unreadable keyset is treated as pin-protected`() = runTest {
        coEvery { core.readKeysetOrNull() } returns null

        val signedOut = repository.recover()

        assertThat(signedOut).isTrue()
        coVerify { auth.sendEvent(AuthEvent.OnSessionExpired) }
        coVerify(exactly = 0) { vault.wipe() }
    }
}
