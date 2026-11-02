package soft.divan.financemanager.core.security.crypto

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class PinKeyDerivationTest {

    private val derivation = PinKeyDerivation()
    private val salt = ByteArray(PinKeyDerivation.SALT_SIZE) { it.toByte() }

    @Test
    fun `same pin and salt give the same key`() {
        val first = derivation.derive("1234".toCharArray(), salt, ITERATIONS)
        val second = derivation.derive("1234".toCharArray(), salt, ITERATIONS)

        assertThat(first.encoded).isEqualTo(second.encoded)
    }

    @Test
    fun `key is aes 256`() {
        val key = derivation.derive("1234".toCharArray(), salt, ITERATIONS)

        assertThat(key.algorithm).isEqualTo("AES")
        assertThat(key.encoded).hasSize(32)
    }

    @Test
    fun `different pin, salt or iterations give a different key`() {
        val base = derivation.derive("1234".toCharArray(), salt, ITERATIONS).encoded

        val otherPin = derivation.derive("1235".toCharArray(), salt, ITERATIONS).encoded
        val otherSalt = derivation.derive("1234".toCharArray(), salt.reversedArray(), ITERATIONS).encoded
        val otherIterations = derivation.derive("1234".toCharArray(), salt, ITERATIONS + 1).encoded

        assertThat(otherPin).isNotEqualTo(base)
        assertThat(otherSalt).isNotEqualTo(base)
        assertThat(otherIterations).isNotEqualTo(base)
    }

    @Test
    fun `caller keeps ownership of the pin array`() {
        val pin = "1234".toCharArray()

        derivation.derive(pin, salt, ITERATIONS)

        assertThat(pin).containsExactly('1', '2', '3', '4')
    }

    @Test
    fun `default iterations stay in the calibrated range`() {
        // Калибровка по замеру на устройстве (docs/encryption.md): меньше — дешевле перебор на
        // взломанном устройстве, больше — разблокировка дольше полсекунды на обычном телефоне
        assertThat(PinKeyDerivation.DEFAULT_ITERATIONS).isBetween(30_000, 150_000)
    }

    private companion object {
        // В тестах итераций мало: проверяется контракт, а не стойкость
        const val ITERATIONS = 1_000
    }
}
