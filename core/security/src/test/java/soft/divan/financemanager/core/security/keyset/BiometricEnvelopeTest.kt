package soft.divan.financemanager.core.security.keyset

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import soft.divan.financemanager.core.security.keystore.AliasedKey
import soft.divan.financemanager.core.security.keystore.FakeKeystoreKeys
import soft.divan.financemanager.core.security.keystore.KeyProtection
import soft.divan.financemanager.core.security.keystore.KeyUnavailableException
import soft.divan.financemanager.core.security.keystore.KeystoreKeys
import javax.crypto.SecretKey

class BiometricEnvelopeTest {

    private val keys = FakeKeystoreKeys()
    private val envelope = BiometricEnvelope(keys)
    private val dek = ByteArray(32) { (it * 3).toByte() }
    private val pinLevel = Keyset(level = KeyLevel.PIN)

    @Test
    fun `enrolled copy unlocks back to the same dek`() {
        val enrollment = envelope.enrollmentCipher()
        val sealed = envelope.finishEnrollment(enrollment, dek)
        val keyset = pinLevel.withBiometric(sealed)

        val cipher = envelope.unlockCipher(keyset)

        assertThat(envelope.finishUnlock(cipher, keyset)).isEqualTo(dek)
        assertThat(sealed.alias).startsWith(DekEnvelope.BIOMETRIC_PREFIX)
        assertThat(keys.protections[sealed.alias]).isEqualTo(KeyProtection.BIOMETRIC)
    }

    @Test
    fun `missing biometric copy is unavailable`() {
        assertThatThrownBy { envelope.unlockCipher(pinLevel) }
            .isInstanceOf(KeyUnavailableException::class.java)
        assertThatThrownBy { envelope.finishUnlock(envelope.enrollmentCipher().cipher, pinLevel) }
            .isInstanceOf(KeyUnavailableException::class.java)
    }

    @Test
    fun `deleted biometric key is unavailable`() {
        val sealed = envelope.finishEnrollment(envelope.enrollmentCipher(), dek)
        keys.delete(sealed.alias)

        assertThatThrownBy { envelope.unlockCipher(pinLevel.withBiometric(sealed)) }
            .isInstanceOf(KeyUnavailableException::class.java)
    }

    @Test
    fun `invalidated biometric key is unavailable`() {
        val sealed = envelope.finishEnrollment(envelope.enrollmentCipher(), dek)
        // Так Keystore ведёт себя после добавления нового отпечатка: ключ есть, но init падает
        keys.keys[sealed.alias] = unusableKey()

        assertThatThrownBy { envelope.unlockCipher(pinLevel.withBiometric(sealed)) }
            .isInstanceOf(KeyUnavailableException::class.java)
            .hasMessageContaining("invalidated")
    }

    @Test
    fun `tampered copy fails on finish`() {
        val sealed = envelope.finishEnrollment(envelope.enrollmentCipher(), dek)
        sealed.blob[sealed.blob.lastIndex] = (sealed.blob.last().toInt() xor 1).toByte()
        val keyset = pinLevel.withBiometric(sealed)

        val cipher = envelope.unlockCipher(keyset)

        assertThatThrownBy { envelope.finishUnlock(cipher, keyset) }
            .isInstanceOf(KeyUnavailableException::class.java)
    }

    @Test
    fun `unusable fresh key is deleted right away`() {
        val keystore = mockk<KeystoreKeys>(relaxUnitFun = true)
        every { keystore.create(any(), any()) } returns AliasedKey("db_kek_bio_x", unusableKey())
        val deleted = mutableListOf<String>()
        every { keystore.delete(capture(deleted)) } returns Unit

        assertThatThrownBy { BiometricEnvelope(keystore).enrollmentCipher() }
            .isInstanceOf(KeyUnavailableException::class.java)
        assertThat(deleted).containsExactly("db_kek_bio_x")
    }

    /** Ключ, с которым `Cipher.init` падает — как инвалидированный ключ Keystore. */
    private fun unusableKey(): SecretKey = mockk {
        every { algorithm } returns "DES"
        every { format } returns "RAW"
        every { encoded } returns ByteArray(3)
    }
}
