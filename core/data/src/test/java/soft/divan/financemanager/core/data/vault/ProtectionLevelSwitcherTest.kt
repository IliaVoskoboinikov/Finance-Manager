package soft.divan.financemanager.core.data.vault

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import soft.divan.financemanager.core.data.testing.VaultHarness
import soft.divan.financemanager.core.domain.model.LocalDataState
import soft.divan.financemanager.core.domain.model.PinUnlockResult
import soft.divan.financemanager.core.domain.model.ProtectionChangeResult
import soft.divan.financemanager.core.security.keyset.DekEnvelope
import soft.divan.financemanager.core.security.keyset.KeyLevel
import soft.divan.financemanager.core.security.keyset.PinAttempts
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ProtectionLevelSwitcherTest {

    @get:Rule
    val folder = TemporaryFolder()

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val dispatcher = StandardTestDispatcher()
    private val harness by lazy {
        VaultHarness(context, File(folder.root, "keyset.preferences_pb"), dispatcher)
    }
    private val switcher get() = harness.switcher

    @Test
    fun `device to open keeps the database key and drops the keystore key`() = switcherTest {
        val deviceAlias = harness.store.read()!!.sealed!!.alias

        val result = switcher.changeLevel(KeyLevel.OPEN, pin = null)

        assertThat(result).isEqualTo(ProtectionChangeResult.Changed)
        assertThat(harness.store.read()!!.level).isEqualTo(KeyLevel.OPEN)
        assertThat(harness.keys.keys).doesNotContainKey(deviceAlias)
        assertThat(reopenedKey()).isEqualTo(harness.openedWith.first())
    }

    @Test
    fun `open back to device creates a fresh keystore key`() = switcherTest {
        switcher.changeLevel(KeyLevel.OPEN, pin = null)

        val result = switcher.changeLevel(KeyLevel.DEVICE, pin = null)

        val keyset = harness.store.read()!!
        assertThat(result).isEqualTo(ProtectionChangeResult.Changed)
        assertThat(keyset.level).isEqualTo(KeyLevel.DEVICE)
        assertThat(keyset.openKey).isNull()
        assertThat(harness.keys.aliases(DekEnvelope.KEY_PREFIX)).containsExactly(keyset.sealed!!.alias)
        assertThat(reopenedKey()).isEqualTo(harness.openedWith.first())
    }

    @Test
    fun `pin level wraps the same database key and resets the failures`() = switcherTest {
        harness.store.writeAttempts(PinAttempts(failures = 2))

        val result = switcher.changeLevel(KeyLevel.PIN, PIN.toCharArray())

        val keyset = harness.store.read()!!
        assertThat(result).isEqualTo(ProtectionChangeResult.Changed)
        assertThat(keyset.level).isEqualTo(KeyLevel.PIN)
        assertThat(keyset.pinIterations).isEqualTo(VaultHarness.PIN_ITERATIONS)
        assertThat(harness.keys.aliases(DekEnvelope.KEY_PREFIX)).containsExactly(keyset.sealed!!.alias)
        assertThat(harness.store.readAttempts()).isEqualTo(PinAttempts())

        harness.vault.lock()
        assertThat(harness.unlocker.unlockWithPin(PIN.toCharArray()))
            .isEqualTo(VaultPinResult.Done(PinUnlockResult.Success))
        assertThat(harness.openedWith.last()).isEqualTo(harness.openedWith.first())
    }

    @Test
    fun `leaving the pin level needs the right pin and is not counted`() = switcherTest {
        switcher.changeLevel(KeyLevel.PIN, PIN.toCharArray())
        enableBiometric()
        val pinKeyset = harness.store.read()!!

        val wrong = switcher.changeLevel(KeyLevel.DEVICE, WRONG.toCharArray())

        assertThat(wrong).isEqualTo(ProtectionChangeResult.WrongPin)
        assertThat(harness.store.read()!!.sealed!!.alias).isEqualTo(pinKeyset.sealed!!.alias)
        assertThat(harness.store.readAttempts()).isEqualTo(PinAttempts())

        val right = switcher.changeLevel(KeyLevel.DEVICE, PIN.toCharArray())

        val keyset = harness.store.read()!!
        assertThat(right).isEqualTo(ProtectionChangeResult.Changed)
        assertThat(keyset.level).isEqualTo(KeyLevel.DEVICE)
        assertThat(keyset.biometric).isNull()
        // Ни внешнего ключа PIN-уровня, ни биометрического больше нет
        assertThat(harness.keys.aliases(DekEnvelope.KEY_PREFIX)).containsExactly(keyset.sealed!!.alias)
    }

    @Test
    fun `pin is required to enter or leave the pin level`() = switcherTest {
        assertThat(switcher.changeLevel(KeyLevel.PIN, pin = null))
            .isEqualTo(ProtectionChangeResult.WrongPin)

        switcher.changeLevel(KeyLevel.PIN, PIN.toCharArray())

        assertThat(switcher.changeLevel(KeyLevel.OPEN, pin = null))
            .isEqualTo(ProtectionChangeResult.WrongPin)
    }

    @Test
    fun `same level is a no-op`() = switcherTest {
        val before = harness.store.read()!!.sealed!!.alias

        assertThat(switcher.changeLevel(KeyLevel.DEVICE, pin = null))
            .isEqualTo(ProtectionChangeResult.Changed)
        assertThat(harness.store.read()!!.sealed!!.alias).isEqualTo(before)
    }

    @Test
    fun `keystore refusal leaves the old keyset intact`() = switcherTest {
        val before = harness.store.read()!!
        harness.keys.failCreate = true

        val result = switcher.changeLevel(KeyLevel.PIN, PIN.toCharArray())

        assertThat(result).isEqualTo(ProtectionChangeResult.Failed)
        assertThat(harness.store.read()!!.sealed!!.alias).isEqualTo(before.sealed!!.alias)
        assertThat(harness.keys.aliases(DekEnvelope.KEY_PREFIX)).containsExactly(before.sealed!!.alias)
    }

    @Test
    fun `lost keystore key fails the change instead of guessing`() = switcherTest {
        harness.keys.delete(harness.store.read()!!.sealed!!.alias)

        assertThat(switcher.changeLevel(KeyLevel.OPEN, pin = null))
            .isEqualTo(ProtectionChangeResult.Failed)
    }

    @Test
    fun `changing the pin rewraps the key and keeps the biometric copy`() = switcherTest {
        switcher.changeLevel(KeyLevel.PIN, PIN.toCharArray())
        enableBiometric()
        val before = harness.store.read()!!

        assertThat(switcher.changePin(WRONG.toCharArray(), NEW_PIN.toCharArray()))
            .isEqualTo(ProtectionChangeResult.WrongPin)
        val result = switcher.changePin(PIN.toCharArray(), NEW_PIN.toCharArray())

        val after = harness.store.read()!!
        assertThat(result).isEqualTo(ProtectionChangeResult.Changed)
        assertThat(after.biometric!!.alias).isEqualTo(before.biometric!!.alias)
        assertThat(harness.keys.keys).doesNotContainKey(before.sealed!!.alias)

        harness.vault.lock()
        assertThat(harness.unlocker.unlockWithPin(PIN.toCharArray()))
            .isInstanceOf(VaultPinResult.Done::class.java)
            .isNotEqualTo(VaultPinResult.Done(PinUnlockResult.Success))
        assertThat(harness.unlocker.unlockWithPin(NEW_PIN.toCharArray()))
            .isEqualTo(VaultPinResult.Done(PinUnlockResult.Success))
    }

    @Test
    fun `changing the pin below the pin level leaves the keyset alone`() = switcherTest {
        val before = harness.store.read()!!.sealed!!.alias

        assertThat(switcher.changePin(PIN.toCharArray(), NEW_PIN.toCharArray()))
            .isEqualTo(ProtectionChangeResult.Changed)
        assertThat(harness.store.read()!!.sealed!!.alias).isEqualTo(before)
    }

    @Test
    fun `no keyset means the change fails`() = switcherTest {
        harness.store.clear()

        assertThat(
            switcher.changeLevel(KeyLevel.OPEN, pin = null)
        ).isEqualTo(ProtectionChangeResult.Failed)
        assertThat(switcher.changePin(PIN.toCharArray(), NEW_PIN.toCharArray()))
            .isEqualTo(ProtectionChangeResult.Failed)
    }

    @Test
    fun `pin arrays are wiped after use`() = switcherTest {
        val pin = PIN.toCharArray()
        val current = PIN.toCharArray()
        val next = NEW_PIN.toCharArray()

        switcher.changeLevel(KeyLevel.PIN, pin)
        switcher.changePin(current, next)

        assertThat(pin).containsOnly(Char.MIN_VALUE)
        assertThat(current).containsOnly(Char.MIN_VALUE)
        assertThat(next).containsOnly(Char.MIN_VALUE)
    }

    @Test
    fun `level change keeps the data open`() = switcherTest {
        switcher.changeLevel(KeyLevel.PIN, PIN.toCharArray())

        assertThat(harness.vault.state.value).isEqualTo(LocalDataState.Open)
    }

    private suspend fun enableBiometric() {
        val start = harness.biometrics.startEnrollment(PIN) as BiometricEnrollmentStart.Ready
        assertThat(harness.biometrics.finishEnrollment(start.session)).isTrue()
    }

    /** Ключ, которым база откроется после перезапуска, — проверяет, что DEK не сменился. */
    private suspend fun reopenedKey(): ByteArray {
        harness.holder.close()
        harness.restart()
        harness.vault.initialize()
        return harness.openedWith.last()
    }

    private fun switcherTest(body: suspend TestScope.() -> Unit) = runTest(dispatcher) {
        try {
            harness.vault.initialize()
            body()
        } finally {
            harness.release()
        }
    }

    private companion object {
        const val PIN = "2468"
        const val NEW_PIN = "8642"
        const val WRONG = "1357"
    }
}
