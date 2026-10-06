package soft.divan.financemanager.core.data.vault

import android.util.Log
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.After
import org.junit.Before
import org.junit.Test
import soft.divan.financemanager.core.data.testing.FakeKeystoreKeys
import soft.divan.financemanager.core.database.holder.DatabaseHolder
import soft.divan.financemanager.core.security.crypto.PinKeyDerivation
import soft.divan.financemanager.core.security.keyset.DekEnvelope
import soft.divan.financemanager.core.security.keyset.KeysetStore
import soft.divan.financemanager.core.security.keystore.KeyUnavailableException
import java.io.IOException

class VaultCoreTest {

    private val keys = FakeKeystoreKeys()
    private val envelope = DekEnvelope(keys, PinKeyDerivation(), pinIterations = 500)
    private val store = mockk<KeysetStore>()
    private val core =
        VaultCore(mockk<DatabaseHolder>(), store, keys, envelope, StandardTestDispatcher())

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.e(any(), any(), any()) } returns 0
    }

    @After
    fun tearDown() {
        unmockkStatic(Log::class)
    }

    @Test
    fun `unreadable or corrupted keyset reads as missing`() = runTest {
        coEvery { store.read() } throws IOException("disk")
        assertThat(core.readKeysetOrNull()).isNull()

        coEvery { store.read() } throws KeyUnavailableException("corrupted")
        assertThat(core.readKeysetOrNull()).isNull()
    }

    @Test
    fun `keyset that does not unseal the key is rejected and its fresh keys dropped`() {
        val dek = envelope.newDek()
        val previous = envelope.sealDevice(dek)
        val next = envelope.sealDevice(envelope.newDek())

        assertThatThrownBy { core.verify(next, dek, pin = null, previous = previous) }
            .isInstanceOf(KeyUnavailableException::class.java)

        assertThat(keys.keys).containsKey(previous.sealed!!.alias)
        assertThat(keys.keys).doesNotContainKey(next.sealed!!.alias)
    }

    @Test
    fun `pin keyset is verified with the pin`() {
        val dek = envelope.newDek()
        val next = envelope.sealPin(dek, "2468".toCharArray())

        core.verify(next, dek, pin = "2468".toCharArray(), previous = null)

        assertThatThrownBy { core.verify(next, dek, pin = "1357".toCharArray(), previous = null) }
            .isInstanceOf(KeyUnavailableException::class.java)
        assertThatThrownBy {
            envelope.sealPin(dek, "2468".toCharArray()).let { core.verify(it, dek, null, null) }
        }
            .isInstanceOf(KeyUnavailableException::class.java)
    }
}
