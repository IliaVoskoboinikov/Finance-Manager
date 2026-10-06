package soft.divan.financemanager.core.data.vault

import android.content.Context
import android.database.sqlite.SQLiteFullException
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
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
import soft.divan.financemanager.core.security.keyset.KeyLevel
import soft.divan.financemanager.core.security.keyset.PinAttempts
import soft.divan.financemanager.core.security.keystore.KeyProtection
import java.io.File
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class BiometricVaultTest {

    @get:Rule
    val folder = TemporaryFolder()

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val dispatcher = StandardTestDispatcher()
    private val harness by lazy {
        VaultHarness(context, File(folder.root, "keyset.preferences_pb"), dispatcher)
    }
    private val biometrics get() = harness.biometrics

    @Test
    fun `nothing to offer below the pin level`() = biometricTest(pinLevel = false) {
        assertThat(biometrics.startUnlock()).isNull()
        assertThat(biometrics.startEnrollment(PIN)).isEqualTo(BiometricEnrollmentStart.Unavailable)
        assertThat(biometrics.observeEnabled().first()).isFalse()
    }

    @Test
    fun `enrollment needs the right pin`() = biometricTest {
        assertThat(biometrics.startEnrollment(WRONG)).isEqualTo(BiometricEnrollmentStart.WrongPin)
        assertThat(harness.keys.protections.values).doesNotContain(KeyProtection.BIOMETRIC)
    }

    @Test
    fun `enrolled copy unlocks a locked database`() = biometricTest {
        enroll()
        harness.store.writeAttempts(PinAttempts(failures = 3))
        harness.vault.lock()
        assertThat(harness.vault.state.value).isEqualTo(LocalDataState.Locked(biometricEnabled = true))

        val session = biometrics.startUnlock()!!
        val unlocked = biometrics.finishUnlock(session)

        assertThat(unlocked).isTrue()
        assertThat(harness.vault.state.value).isEqualTo(LocalDataState.Open)
        assertThat(harness.store.readAttempts()).isEqualTo(PinAttempts())
        assertThat(harness.openedWith.last()).isEqualTo(harness.openedWith.first())
    }

    @Test
    fun `database that fails to open after biometric auth leads to recovery`() = biometricTest {
        enroll()
        harness.vault.lock()
        harness.failNextOpen = SQLiteFullException("database or disk is full")

        val unlocked = biometrics.finishUnlock(biometrics.startUnlock()!!)

        assertThat(unlocked).isFalse()
        assertThat(harness.vault.state.value).isEqualTo(LocalDataState.KeyLost)
        // Копия ни при чём — сбой в файле базы, а не в ключе
        assertThat(harness.store.read()!!.biometric).isNotNull()
    }

    @Test
    fun `new fingerprint invalidates the copy but keeps the data`() = biometricTest {
        enroll()
        harness.vault.lock()
        harness.keys.invalidate(harness.store.read()!!.biometric!!.alias)

        val session = biometrics.startUnlock()

        assertThat(session).isNull()
        assertThat(harness.store.read()!!.biometric).isNull()
        assertThat(harness.store.read()!!.level).isEqualTo(KeyLevel.PIN)
        assertThat(harness.vault.state.value).isEqualTo(LocalDataState.Locked(biometricEnabled = false))
        assertThat(harness.unlocker.unlockWithPin(PIN.toCharArray()))
            .isEqualTo(VaultPinResult.Done(PinUnlockResult.Success))
    }

    @Test
    fun `stale or used sessions do not unlock`() = biometricTest {
        enroll()
        harness.vault.lock()
        val session = biometrics.startUnlock()!!
        assertThat(biometrics.finishUnlock(session)).isTrue()

        // Второй раз той же сессией — нельзя
        assertThat(biometrics.finishUnlock(session)).isFalse()

        // Копию пересоздали — старая сессия ссылается на удалённый ключ
        val stale = biometrics.startUnlock()!!
        biometrics.disable()
        enroll()
        assertThat(biometrics.finishUnlock(stale)).isFalse()
    }

    @Test
    fun `failed unlock keeps the copy for another try`() = biometricTest {
        enroll()
        harness.vault.lock()
        val alias = harness.store.read()!!.biometric!!.alias
        val broken = BiometricSession(unauthenticatedCipher(), alias, dek = null)

        assertThat(biometrics.finishUnlock(broken)).isFalse()

        assertThat(harness.store.read()!!.biometric!!.alias).isEqualTo(alias)
        assertThat(harness.vault.state.value).isInstanceOf(LocalDataState.Locked::class.java)
    }

    @Test
    fun `cancelled enrollment leaves nothing behind`() = biometricTest {
        val start = biometrics.startEnrollment(PIN) as BiometricEnrollmentStart.Ready

        biometrics.cancel(start.session)

        assertThat(harness.keys.keys).doesNotContainKey(start.session.alias)
        assertThat(start.session.dek).containsOnly(0)
        assertThat(harness.store.read()!!.biometric).isNull()
        assertThat(biometrics.finishEnrollment(start.session)).isFalse()
    }

    @Test
    fun `cancelling a finished session keeps the saved copy`() = biometricTest {
        val start = biometrics.startEnrollment(PIN) as BiometricEnrollmentStart.Ready
        biometrics.finishEnrollment(start.session)

        biometrics.cancel(start.session)

        assertThat(harness.keys.keys).containsKey(start.session.alias)
        assertThat(harness.store.read()!!.biometric!!.alias).isEqualTo(start.session.alias)
    }

    @Test
    fun `failed sealing drops the fresh key`() = biometricTest {
        val start = biometrics.startEnrollment(PIN) as BiometricEnrollmentStart.Ready
        val broken = BiometricSession(unauthenticatedCipher(), start.session.alias, start.session.dek)

        assertThat(biometrics.finishEnrollment(broken)).isFalse()

        assertThat(harness.keys.keys).doesNotContainKey(start.session.alias)
        assertThat(harness.store.read()!!.biometric).isNull()
    }

    @Test
    fun `re-enrolling replaces the previous copy`() = biometricTest {
        enroll()
        val first = harness.store.read()!!.biometric!!.alias

        enroll()

        val second = harness.store.read()!!.biometric!!.alias
        assertThat(second).isNotEqualTo(first)
        assertThat(harness.keys.keys).doesNotContainKey(first)
    }

    @Test
    fun `disable removes the copy and its key`() = biometricTest {
        enroll()
        val alias = harness.store.read()!!.biometric!!.alias
        assertThat(biometrics.observeEnabled().first()).isTrue()

        biometrics.disable()
        biometrics.disable()

        assertThat(harness.store.read()!!.biometric).isNull()
        assertThat(harness.keys.keys).doesNotContainKey(alias)
        assertThat(biometrics.observeEnabled().first()).isFalse()
    }

    @Test
    fun `keystore refusing the biometric key makes it unavailable`() = biometricTest {
        harness.keys.failCreate = true

        assertThat(biometrics.startEnrollment(PIN)).isEqualTo(BiometricEnrollmentStart.Unavailable)
    }

    @Test
    fun `lost pin-level key makes enrollment unavailable`() = biometricTest {
        harness.keys.delete(harness.store.read()!!.sealed!!.alias)

        assertThat(biometrics.startEnrollment(PIN)).isEqualTo(BiometricEnrollmentStart.Unavailable)
    }

    private suspend fun enroll() {
        val start = biometrics.startEnrollment(PIN) as BiometricEnrollmentStart.Ready
        assertThat(biometrics.finishEnrollment(start.session)).isTrue()
    }

    /** Шифр, на котором `doFinal` падает, — как при сбое биометрической аутентификации. */
    private fun unauthenticatedCipher(): Cipher {
        val key = KeyGenerator.getInstance("AES").apply { init(256) }.generateKey()
        return Cipher.getInstance("AES/GCM/NoPadding").apply {
            init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, ByteArray(12)))
        }
    }

    private fun biometricTest(pinLevel: Boolean = true, body: suspend TestScope.() -> Unit) =
        runTest(dispatcher) {
            try {
                harness.vault.initialize()
                if (pinLevel) harness.switcher.changeLevel(KeyLevel.PIN, PIN.toCharArray())
                body()
            } finally {
                harness.release()
            }
        }

    private companion object {
        const val PIN = "2468"
        const val WRONG = "1357"
    }
}
