package soft.divan.financemanager.core.database.holder

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test

class RawKeyTest {

    @Test
    fun `key becomes a sqlcipher raw key literal`() {
        val key = ByteArray(RawKey.KEY_SIZE) { it.toByte() }

        val passphrase = String(RawKey.passphrase(key), Charsets.US_ASCII)

        assertThat(passphrase)
            .isEqualTo("x'000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f'")
    }

    @Test
    fun `high bytes are encoded without sign extension`() {
        val key = ByteArray(RawKey.KEY_SIZE) { 0xFF.toByte() }

        val passphrase = String(RawKey.passphrase(key), Charsets.US_ASCII)

        assertThat(passphrase).isEqualTo("x'" + "ff".repeat(RawKey.KEY_SIZE) + "'")
    }

    @Test
    fun `literal length is what sqlcipher expects for a 256-bit raw key`() {
        // SQLCipher распознаёт готовый ключ по длине: 2 * 32 hex-символа + x, две кавычки
        assertThat(RawKey.passphrase(ByteArray(RawKey.KEY_SIZE))).hasSize(67)
    }

    @Test
    fun `source key is left untouched`() {
        val key = ByteArray(RawKey.KEY_SIZE) { 7 }

        RawKey.passphrase(key)

        assertThat(key).containsOnly(7)
    }

    @Test
    fun `keys of the wrong size are rejected`() {
        assertThatThrownBy { RawKey.passphrase(ByteArray(16)) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }
}
