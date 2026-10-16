package soft.divan.financemanager.core.security.keyset

import android.os.SystemClock
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import soft.divan.financemanager.core.security.crypto.AesGcm
import soft.divan.financemanager.core.security.crypto.PinKeyDerivation
import soft.divan.financemanager.core.security.keystore.KeyProtection
import soft.divan.financemanager.core.security.keystore.KeyUnavailableException
import soft.divan.financemanager.core.security.keystore.impl.AndroidKeystoreKeys
import java.security.GeneralSecurityException
import java.security.KeyStore

/**
 * Заворачивание ключа базы настоящим Android Keystore — в JVM его нет, а параметры ключей
 * (запрет использования на заблокированном экране, биометрия) проверяет только сам Keystore.
 */
@RunWith(AndroidJUnit4::class)
class KeystoreEnvelopeTest {

    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
    private val keys = AndroidKeystoreKeys(keyStore)
    private val envelope = DekEnvelope(keys, PinKeyDerivation(), pinIterations = TEST_ITERATIONS)

    @After
    fun cleanup() {
        keys.aliases(DekEnvelope.KEY_PREFIX).forEach(keys::delete)
    }

    @Test
    fun deviceLevelRoundTrip() {
        val dek = envelope.newDek()

        val keyset = envelope.sealDevice(dek)

        assertArrayEquals(dek, envelope.openWithoutUser(keyset))
    }

    @Test
    fun pinLevelOpensOnlyWithThePin() {
        val dek = envelope.newDek()

        val keyset = envelope.sealPin(dek, "2468".toCharArray())

        assertArrayEquals(dek, envelope.openWithPin(keyset, "2468".toCharArray()))
        assertNull(envelope.openWithPin(keyset, "1357".toCharArray()))
    }

    @Test
    fun keystoreKeyIsNotExtractable() {
        val created = keys.create(DekEnvelope.DEVICE_PREFIX, KeyProtection.ALWAYS_AVAILABLE)

        // Ключ Keystore живёт в защищённом железе/процессе: сырых байт у него нет
        assertNull(created.key.encoded)
    }

    @Test
    fun deletedKeystoreKeyIsReportedUnavailable() {
        val keyset = envelope.sealDevice(envelope.newDek())
        keys.delete(keyset.sealed!!.alias)

        try {
            envelope.openWithoutUser(keyset)
            fail("Opened a keyset whose Keystore key is gone")
        } catch (expected: KeyUnavailableException) {
            Log.i(TAG, "Lost key reported: ${expected.message}")
        }
    }

    @Test
    fun biometricKeyDoesNotWorkWithoutAuthentication() {
        val created = try {
            keys.create(DekEnvelope.BIOMETRIC_PREFIX, KeyProtection.BIOMETRIC)
        } catch (expected: KeyUnavailableException) {
            // Без защищённой блокировки экрана Keystore такой ключ не создаёт вовсе
            Log.i(TAG, "Biometric key unavailable on this device: ${expected.cause}")
            null
        }
        assumeTrue("No secure lock screen with biometrics", created != null)

        try {
            AesGcm.encrypt(created!!.key, ByteArray(32))
            fail("Biometric key worked without authentication")
        } catch (expected: GeneralSecurityException) {
            Log.i(TAG, "Biometric key refused: ${expected.javaClass.simpleName}")
        }
    }

    @Test
    fun pinDerivationCalibration() {
        val derivation = PinKeyDerivation()
        val salt = ByteArray(PinKeyDerivation.SALT_SIZE)

        val started = SystemClock.elapsedRealtime()
        derivation.derive("2468".toCharArray(), salt, PinKeyDerivation.DEFAULT_ITERATIONS)
        val elapsed = SystemClock.elapsedRealtime() - started

        Log.i(TAG, "PBKDF2 ${PinKeyDerivation.DEFAULT_ITERATIONS} iterations took $elapsed ms")
        assertTrue("PBKDF2 took $elapsed ms", elapsed < DERIVATION_BUDGET_MS)
    }

    private companion object {
        const val TAG = "KeystoreEnvelopeTest"
        const val TEST_ITERATIONS = 10_000

        /** С запасом на медленные устройства; цель калибровки — около 500 мс. */
        const val DERIVATION_BUDGET_MS = 2_000L
    }
}
