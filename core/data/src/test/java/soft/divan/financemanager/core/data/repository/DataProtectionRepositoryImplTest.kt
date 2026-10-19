package soft.divan.financemanager.core.data.repository

import android.util.Log
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import soft.divan.financemanager.core.auth.domain.model.AuthStatus
import soft.divan.financemanager.core.auth.domain.provider.AuthStateProvider
import soft.divan.financemanager.core.data.outbox.OutboxProcessor
import soft.divan.financemanager.core.data.source.OutboxLocalDataSource
import soft.divan.financemanager.core.data.vault.ProtectionLevelSwitcher
import soft.divan.financemanager.core.data.vault.VaultCore
import soft.divan.financemanager.core.domain.model.DataProtectionLevel
import soft.divan.financemanager.core.domain.model.ProtectionChangeResult
import soft.divan.financemanager.core.security.keyset.KeyLevel
import soft.divan.financemanager.core.security.keyset.Keyset
import soft.divan.financemanager.core.security.keyset.KeysetStore
import java.io.IOException

class DataProtectionRepositoryImplTest {

    private val switcher = mockk<ProtectionLevelSwitcher>()
    private val store = mockk<KeysetStore>()
    private val core =
        mockk<VaultCore> { every { store } returns this@DataProtectionRepositoryImplTest.store }
    private val auth = mockk<AuthStateProvider>()
    private val processor = mockk<OutboxProcessor>()
    private val outbox = mockk<OutboxLocalDataSource>()

    private val repository = DataProtectionRepositoryImpl(switcher, core, auth, processor, outbox)

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.w(any(), any<String>(), any()) } returns 0
    }

    @After
    fun tearDown() {
        unmockkStatic(Log::class)
    }

    @Test
    fun `level is mapped and silent until keys exist`() = runTest {
        every { store.observeLevel() } returns flowOf(null, KeyLevel.OPEN, KeyLevel.DEVICE, KeyLevel.PIN)

        assertThat(repository.observeLevel().toList()).containsExactly(
            DataProtectionLevel.NONE,
            DataProtectionLevel.DEVICE,
            DataProtectionLevel.PIN
        )
    }

    @Test
    fun `current level is read from the keyset or unknown`() = runTest {
        coEvery { core.readKeysetOrNull() } returns Keyset(level = KeyLevel.OPEN)
        assertThat(repository.currentLevel()).isEqualTo(DataProtectionLevel.NONE)

        coEvery { core.readKeysetOrNull() } returns null
        assertThat(repository.currentLevel()).isNull()
    }

    @Test
    fun `pin level is refused while changes are still unsent`() = runTest {
        every { auth.observeStatus() } returns flowOf(AuthStatus.AUTHORIZED)
        coEvery { processor.process() } returns false
        coEvery { outbox.countUnsent() } returns 2

        val result = repository.changeLevel(DataProtectionLevel.PIN, "1234")

        assertThat(result).isEqualTo(ProtectionChangeResult.UnsentChanges)
        coVerify { processor.process() }
        coVerify(exactly = 0) { switcher.changeLevel(any(), any()) }
    }

    @Test
    fun `pin level is enabled once the queue is drained`() = runTest {
        every { auth.observeStatus() } returns flowOf(AuthStatus.AUTHORIZED)
        coEvery { processor.process() } returns true
        coEvery { outbox.countUnsent() } returns 0
        coEvery { switcher.changeLevel(KeyLevel.PIN, any()) } returns ProtectionChangeResult.Changed

        assertThat(repository.changeLevel(DataProtectionLevel.PIN, "1234"))
            .isEqualTo(ProtectionChangeResult.Changed)
        coVerify { switcher.changeLevel(KeyLevel.PIN, match { String(it!!) == "1234" }) }
    }

    @Test
    fun `failed drain counts as unsent`() = runTest {
        every { auth.observeStatus() } returns flowOf(AuthStatus.AUTHORIZED)
        coEvery { processor.process() } throws IOException("offline")

        assertThat(repository.changeLevel(DataProtectionLevel.PIN, "1234"))
            .isEqualTo(ProtectionChangeResult.UnsentChanges)
    }

    @Test
    fun `guest has no server to drain to`() = runTest {
        every { auth.observeStatus() } returns flowOf(AuthStatus.GUEST)
        coEvery { switcher.changeLevel(KeyLevel.PIN, any()) } returns ProtectionChangeResult.Changed

        assertThat(repository.changeLevel(DataProtectionLevel.PIN, "1234"))
            .isEqualTo(ProtectionChangeResult.Changed)
        coVerify(exactly = 0) { processor.process() }
    }

    @Test
    fun `lower levels need no drain`() = runTest {
        coEvery { switcher.changeLevel(KeyLevel.OPEN, null) } returns ProtectionChangeResult.Changed

        assertThat(repository.changeLevel(DataProtectionLevel.NONE, null))
            .isEqualTo(ProtectionChangeResult.Changed)
        coVerify(exactly = 0) { processor.process() }
    }

    @Test
    fun `pin change goes to the switcher`() = runTest {
        coEvery { switcher.changePin(any(), any()) } returns ProtectionChangeResult.WrongPin

        assertThat(repository.changePin("1111", "2222")).isEqualTo(ProtectionChangeResult.WrongPin)
        coVerify {
            switcher.changePin(match { String(it) == "1111" }, match { String(it) == "2222" })
        }
    }
}
