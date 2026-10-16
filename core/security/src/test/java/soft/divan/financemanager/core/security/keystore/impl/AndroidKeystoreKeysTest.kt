package soft.divan.financemanager.core.security.keystore.impl

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import soft.divan.financemanager.core.security.keystore.KeyProtection
import soft.divan.financemanager.core.security.keystore.KeyUnavailableException
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.ProviderException
import java.security.UnrecoverableKeyException
import java.security.spec.AlgorithmParameterSpec
import java.util.Collections
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

/**
 * Параметры создаваемых ключей и обработка ошибок Keystore.
 *
 * Настоящего Android Keystore в JVM нет, поэтому генератор и хранилище подменены: проверяется то,
 * что класс **просит** у Keystore (спецификация ключа), и то, как он реагирует на отказы.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AndroidKeystoreKeysTest {

    private val stored = linkedMapOf<String, SecretKey>()
    private val specs = mutableListOf<KeyGenParameterSpec>()

    /** Сколько первых попыток генерации упадёт — как на прошивке без нужного режима. */
    private var failuresLeft = 0
    private var failure: Exception = ProviderException("Keystore refused the spec")

    private val keyStore: KeyStore = mockk(relaxed = true) {
        every { getKey(any(), any()) } answers { stored[firstArg()] }
        every { aliases() } answers { Collections.enumeration(stored.keys.toList()) }
        every { deleteEntry(any()) } answers { stored.remove(firstArg<String>()) }
    }

    private val keys = AndroidKeystoreKeys(keyStore) { newGenerator() }

    @Test
    fun `always available key needs no user authentication`() {
        val created = keys.create("db_kek_device", KeyProtection.ALWAYS_AVAILABLE)
        val spec = specs.single()

        assertThat(created.alias).startsWith("db_kek_device_")
        assertThat(spec.keystoreAlias).isEqualTo(created.alias)
        assertThat(spec.purposes)
            .isEqualTo(KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
        assertThat(spec.blockModes).containsExactly(KeyProperties.BLOCK_MODE_GCM)
        assertThat(spec.encryptionPaddings).containsExactly(KeyProperties.ENCRYPTION_PADDING_NONE)
        assertThat(spec.isRandomizedEncryptionRequired).isTrue()
        assertThat(spec.keySize).isEqualTo(256)
        assertThat(spec.isUserAuthenticationRequired).isFalse()
        assertThat(spec.isUnlockedDeviceRequired).isFalse()
    }

    @Test
    fun `unlocked device key is unusable on the lock screen`() {
        keys.create("db_kek_pin", KeyProtection.UNLOCKED_DEVICE)

        assertThat(specs.single().isUnlockedDeviceRequired).isTrue()
        assertThat(specs.single().isUserAuthenticationRequired).isFalse()
    }

    @Test
    fun `unlocked device key falls back to a plain keystore key where unsupported`() {
        failuresLeft = 1

        val created = keys.create("db_kek_pin", KeyProtection.UNLOCKED_DEVICE)

        assertThat(specs).hasSize(2)
        assertThat(specs.last().isUnlockedDeviceRequired).isFalse()
        assertThat(specs.last().keystoreAlias).isEqualTo(created.alias)
        assertThat(stored).containsKey(created.alias)
    }

    @Test
    fun `unlocked device key fails when even the fallback is refused`() {
        failuresLeft = 2

        assertThatThrownBy { keys.create("db_kek_pin", KeyProtection.UNLOCKED_DEVICE) }
            .isInstanceOf(KeyUnavailableException::class.java)
    }

    @Test
    fun `unlocked device fallback also covers checked keystore errors`() {
        failuresLeft = 2
        failure = java.security.InvalidAlgorithmParameterException("no secure lock screen")

        assertThatThrownBy { keys.create("db_kek_pin", KeyProtection.UNLOCKED_DEVICE) }
            .isInstanceOf(KeyUnavailableException::class.java)
        assertThat(specs).hasSize(2)
    }

    @Test
    fun `biometric key needs strong biometrics for every use`() {
        keys.create("db_kek_bio", KeyProtection.BIOMETRIC)
        val spec = specs.single()

        assertThat(spec.isUserAuthenticationRequired).isTrue()
        assertThat(spec.isInvalidatedByBiometricEnrollment).isTrue()
        assertThat(spec.userAuthenticationValidityDurationSeconds).isZero()
        assertThat(spec.userAuthenticationType).isEqualTo(KeyProperties.AUTH_BIOMETRIC_STRONG)
    }

    @Test
    fun `other protections do not fall back`() {
        failuresLeft = 1
        assertThatThrownBy { keys.create("db_kek_bio", KeyProtection.BIOMETRIC) }
            .isInstanceOf(KeyUnavailableException::class.java)

        failuresLeft = 1
        assertThatThrownBy { keys.create("db_kek_device", KeyProtection.ALWAYS_AVAILABLE) }
            .isInstanceOf(KeyUnavailableException::class.java)
            .hasCauseInstanceOf(ProviderException::class.java)

        // Без второй попытки: одна спецификация на каждый вызов
        assertThat(specs).hasSize(2)
    }

    @Test
    fun `checked keystore error on creation is unavailable`() {
        failuresLeft = 1
        failure = java.security.InvalidAlgorithmParameterException("bad spec")

        assertThatThrownBy { keys.create("db_kek_device", KeyProtection.ALWAYS_AVAILABLE) }
            .isInstanceOf(KeyUnavailableException::class.java)
    }

    @Test
    fun `aliases are unique per creation`() {
        val first = keys.create("db_kek_device", KeyProtection.ALWAYS_AVAILABLE)
        val second = keys.create("db_kek_device", KeyProtection.ALWAYS_AVAILABLE)

        assertThat(first.alias).isNotEqualTo(second.alias)
    }

    @Test
    fun `find returns the stored key or null`() {
        val created = keys.create("db_kek_device", KeyProtection.ALWAYS_AVAILABLE)

        assertThat(keys.find(created.alias)).isSameAs(created.key)
        assertThat(keys.find("missing")).isNull()
    }

    @Test
    fun `unreadable key is unavailable, not missing`() {
        every { keyStore.getKey("broken", any()) } throws UnrecoverableKeyException("broken")

        assertThatThrownBy { keys.find("broken") }
            .isInstanceOf(KeyUnavailableException::class.java)
    }

    @Test
    fun `aliases are filtered by prefix`() {
        val device = keys.create("db_kek_device", KeyProtection.ALWAYS_AVAILABLE)
        val pin = keys.create("db_kek_pin", KeyProtection.UNLOCKED_DEVICE)
        stored["pin_alias"] = device.key

        assertThat(keys.aliases("db_kek_")).containsExactlyInAnyOrder(device.alias, pin.alias)
    }

    @Test
    fun `listing failure yields no aliases`() {
        every { keyStore.aliases() } throws KeyStoreException("not initialized")

        assertThat(keys.aliases("db_kek_")).isEmpty()
    }

    @Test
    fun `delete removes the entry and tolerates failures`() {
        val created = keys.create("db_kek_device", KeyProtection.ALWAYS_AVAILABLE)

        keys.delete(created.alias)
        assertThat(stored).doesNotContainKey(created.alias)

        every { keyStore.deleteEntry("stuck") } throws KeyStoreException("busy")
        keys.delete("stuck")
        verify { keyStore.deleteEntry("stuck") }
    }

    private fun newGenerator(): KeyGenerator {
        var pending: KeyGenParameterSpec? = null
        return mockk {
            every { init(any<AlgorithmParameterSpec>()) } answers {
                pending = firstArg<KeyGenParameterSpec>().also(specs::add)
            }
            every { generateKey() } answers {
                if (failuresLeft > 0) {
                    failuresLeft--
                    throw failure
                }
                val key = KeyGenerator.getInstance("AES").apply { init(256) }.generateKey()
                key.also { stored[pending!!.keystoreAlias] = it }
            }
        }
    }
}
