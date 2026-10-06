package soft.divan.financemanager.core.security.keyset

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import soft.divan.financemanager.core.security.crypto.AesGcm
import soft.divan.financemanager.core.security.crypto.PinKeyDerivation
import soft.divan.financemanager.core.security.keystore.FakeKeystoreKeys
import soft.divan.financemanager.core.security.keystore.KeyProtection
import soft.divan.financemanager.core.security.keystore.KeyUnavailableException
import javax.crypto.AEADBadTagException

class DekEnvelopeTest {

    private val keys = FakeKeystoreKeys()
    private val derivation = PinKeyDerivation()
    private val envelope = DekEnvelope(keys, derivation)
    private val dek = envelope.newDek()

    @Test
    fun `new dek is 32 random bytes`() {
        assertThat(dek).hasSize(32)
        assertThat(envelope.newDek()).isNotEqualTo(dek)
    }

    @Test
    fun `open level stores a copy of the dek`() {
        val keyset = envelope.sealOpen(dek)

        assertThat(keyset.level).isEqualTo(KeyLevel.OPEN)
        assertThat(keyset.aliases()).isEmpty()
        assertThat(envelope.openWithoutUser(keyset)).isEqualTo(dek)

        // Копия, а не ссылка: вызывающий обнуляет свой массив, а набор остаётся рабочим
        dek.fill(0)
        assertThat(envelope.openWithoutUser(keyset)).isNotEqualTo(dek)
    }

    @Test
    fun `device level wraps the dek with an always-available keystore key`() {
        val keyset = envelope.sealDevice(dek)

        assertThat(keyset.level).isEqualTo(KeyLevel.DEVICE)
        assertThat(keyset.openKey).isNull()
        assertThat(keyset.sealed!!.alias).startsWith(DekEnvelope.DEVICE_PREFIX)
        assertThat(keys.protections[keyset.sealed!!.alias]).isEqualTo(KeyProtection.ALWAYS_AVAILABLE)
        assertThat(envelope.openWithoutUser(keyset)).isEqualTo(dek)
    }

    @Test
    fun `every seal gets its own keystore alias`() {
        val first = envelope.sealDevice(dek)
        val second = envelope.sealDevice(dek)

        assertThat(first.sealed!!.alias).isNotEqualTo(second.sealed!!.alias)
    }

    @Test
    fun `pin level opens with the right pin only`() {
        val keyset = envelope.sealPin(dek, "1234".toCharArray(), iterations = ITERATIONS)

        assertThat(keyset.level).isEqualTo(KeyLevel.PIN)
        assertThat(keyset.pinIterations).isEqualTo(ITERATIONS)
        assertThat(keyset.pinSalt).hasSize(PinKeyDerivation.SALT_SIZE)
        assertThat(keys.protections[keyset.sealed!!.alias]).isEqualTo(KeyProtection.UNLOCKED_DEVICE)
        assertThat(envelope.openWithPin(keyset, "1234".toCharArray())).isEqualTo(dek)
        assertThat(envelope.openWithPin(keyset, "4321".toCharArray())).isNull()
    }

    @Test
    fun `pin level uses the configured iteration count by default`() {
        val fast = DekEnvelope(keys, derivation, pinIterations = 500)

        val keyset = fast.sealPin(dek, "1234".toCharArray())

        assertThat(keyset.pinIterations).isEqualTo(500)
        assertThat(fast.openWithPin(keyset, "1234".toCharArray())).isEqualTo(dek)
    }

    @Test
    fun `injected envelope uses the production iteration count`() {
        val keyset = envelope.sealPin(dek, "1234".toCharArray())

        assertThat(keyset.pinIterations).isEqualTo(PinKeyDerivation.DEFAULT_ITERATIONS)
    }

    @Test
    fun `pin cannot be checked without the keystore key`() {
        val keyset = envelope.sealPin(dek, "1234".toCharArray(), iterations = ITERATIONS)
        val pinKey = derivation.derive("1234".toCharArray(), keyset.pinSalt!!, ITERATIONS)

        // Злоумышленник скопировал файлы и знает верный PIN — но снаружи слой Keystore, и ключ из
        // PIN его не снимает. Значит, перебор PIN без телефона ничего не проверяет.
        assertThatThrownBy { AesGcm.decrypt(pinKey, keyset.sealed!!.blob) }
            .isInstanceOf(AEADBadTagException::class.java)
    }

    @Test
    fun `lost keystore key is reported as unavailable, not as a wrong pin`() {
        val keyset = envelope.sealPin(dek, "1234".toCharArray(), iterations = ITERATIONS)
        keys.delete(keyset.sealed!!.alias)

        assertThatThrownBy { envelope.openWithPin(keyset, "1234".toCharArray()) }
            .isInstanceOf(KeyUnavailableException::class.java)
    }

    @Test
    fun `corrupted outer layer is reported as unavailable`() {
        val keyset = envelope.sealDevice(dek)
        val blob = keyset.sealed!!.blob
        blob[blob.lastIndex] = (blob.last().toInt() xor 1).toByte()

        assertThatThrownBy { envelope.openWithoutUser(keyset) }
            .isInstanceOf(KeyUnavailableException::class.java)
    }

    @Test
    fun `pin level carries the biometric copy over`() {
        val biometric = SealedKey("db_kek_bio_1", byteArrayOf(1, 2, 3))

        val keyset = envelope.sealPin(dek, "1234".toCharArray(), biometric, ITERATIONS)

        assertThat(keyset.biometric).isSameAs(biometric)
        assertThat(keyset.aliases()).containsExactlyInAnyOrder(keyset.sealed!!.alias, biometric.alias)
    }

    @Test
    fun `pin level needs the pin to open`() {
        val keyset = envelope.sealPin(dek, "1234".toCharArray(), iterations = ITERATIONS)

        assertThatThrownBy { envelope.openWithoutUser(keyset) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `pin is rejected for levels without a pin`() {
        assertThatThrownBy { envelope.openWithPin(envelope.sealDevice(dek), "1234".toCharArray()) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `incomplete keysets are reported as unavailable`() {
        val noOpenKey = Keyset(level = KeyLevel.OPEN)
        val noSealed = Keyset(level = KeyLevel.DEVICE)
        val noSalt = Keyset(level = KeyLevel.PIN, sealed = SealedKey("a", ByteArray(40)))

        assertThatThrownBy { envelope.openWithoutUser(noOpenKey) }
            .isInstanceOf(KeyUnavailableException::class.java)
        assertThatThrownBy { envelope.openWithoutUser(noSealed) }
            .isInstanceOf(KeyUnavailableException::class.java)
        assertThatThrownBy { envelope.openWithPin(noSalt, "1234".toCharArray()) }
            .isInstanceOf(KeyUnavailableException::class.java)
    }

    @Test
    fun `failure to create the keystore key leaves nothing behind`() {
        keys.failCreate = true

        assertThatThrownBy { envelope.sealPin(dek, "1234".toCharArray(), iterations = ITERATIONS) }
            .isInstanceOf(KeyUnavailableException::class.java)
        assertThat(keys.keys).isEmpty()
    }

    @Test
    fun `keyset helpers swap only the biometric copy`() {
        val keyset = envelope.sealPin(dek, "1234".toCharArray(), iterations = ITERATIONS)
        val biometric = SealedKey("db_kek_bio_7", byteArrayOf(7))

        val withBio = keyset.withBiometric(biometric)
        val withoutBio = withBio.withoutBiometric()

        assertThat(withBio.biometric).isSameAs(biometric)
        assertThat(withBio.sealed).isSameAs(keyset.sealed)
        assertThat(withoutBio.biometric).isNull()
        assertThat(withoutBio.pinSalt).isSameAs(keyset.pinSalt)
        assertThat(withoutBio.pinIterations).isEqualTo(ITERATIONS)
    }

    private companion object {
        const val ITERATIONS = 1_000
    }
}
