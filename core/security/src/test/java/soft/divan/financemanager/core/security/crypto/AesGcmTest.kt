package soft.divan.financemanager.core.security.crypto

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import java.security.GeneralSecurityException
import javax.crypto.AEADBadTagException
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class AesGcmTest {

    private val key = newKey()
    private val secret = ByteArray(32) { it.toByte() }

    @Test
    fun `decrypt returns what encrypt sealed`() {
        val blob = AesGcm.encrypt(key, secret)

        assertThat(AesGcm.decrypt(key, blob)).isEqualTo(secret)
    }

    @Test
    fun `blob is iv plus ciphertext plus tag`() {
        val blob = AesGcm.encrypt(key, secret)

        assertThat(blob).hasSize(IV_SIZE + secret.size + TAG_SIZE)
    }

    @Test
    fun `each encryption uses a fresh iv`() {
        val first = AesGcm.encrypt(key, secret)
        val second = AesGcm.encrypt(key, secret)

        assertThat(first.copyOfRange(0, IV_SIZE)).isNotEqualTo(second.copyOfRange(0, IV_SIZE))
        assertThat(first).isNotEqualTo(second)
    }

    @Test
    fun `wrong key fails the tag check instead of returning garbage`() {
        val blob = AesGcm.encrypt(key, secret)

        assertThatThrownBy { AesGcm.decrypt(newKey(), blob) }
            .isInstanceOf(AEADBadTagException::class.java)
    }

    @Test
    fun `tampered blob fails the tag check`() {
        val blob = AesGcm.encrypt(key, secret)
        blob[blob.lastIndex] = (blob.last().toInt() xor 1).toByte()

        assertThatThrownBy { AesGcm.decrypt(key, blob) }
            .isInstanceOf(AEADBadTagException::class.java)
    }

    @Test
    fun `too short blob is rejected before touching the cipher`() {
        assertThatThrownBy { AesGcm.decrypt(key, ByteArray(IV_SIZE + TAG_SIZE - 1)) }
            .isInstanceOf(GeneralSecurityException::class.java)
            .hasMessageContaining("too short")
        assertThatThrownBy { AesGcm.finishDecrypt(AesGcm.encryptCipher(key), ByteArray(3)) }
            .isInstanceOf(GeneralSecurityException::class.java)
    }

    @Test
    fun `split cipher steps interoperate with one-shot calls`() {
        val blob = AesGcm.finishEncrypt(AesGcm.encryptCipher(key), secret)
        val cipher = AesGcm.decryptCipher(key, blob)

        assertThat(AesGcm.finishDecrypt(cipher, blob)).isEqualTo(secret)
        assertThat(AesGcm.decrypt(key, AesGcm.encrypt(key, secret))).isEqualTo(secret)
    }

    private fun newKey(): SecretKey =
        KeyGenerator.getInstance("AES").apply { init(256) }.generateKey()

    private companion object {
        const val IV_SIZE = 12
        const val TAG_SIZE = 16
    }
}
