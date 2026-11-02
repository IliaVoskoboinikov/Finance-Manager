package soft.divan.financemanager.core.security.keystore

import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

/**
 * Программный заменитель Android Keystore для JVM-тестов: те же контракты, ключи — обычные AES.
 *
 * Провайдера `AndroidKeyStore` в JVM нет, а неподписанный JCE-провайдер Oracle JDK не пустит,
 * поэтому подменяется интерфейс целиком.
 */
class FakeKeystoreKeys : KeystoreKeys {

    val keys = linkedMapOf<String, SecretKey>()
    val protections = mutableMapOf<String, KeyProtection>()

    /** Создание ключа падает — как на прошивке без поддержки нужного режима. */
    var failCreate = false

    private var counter = 0

    override fun find(alias: String): SecretKey? = keys[alias]

    override fun create(prefix: String, protection: KeyProtection): AliasedKey {
        if (failCreate) throw KeyUnavailableException("Key creation is disabled")
        val alias = "${prefix}_${++counter}"
        val key = KeyGenerator.getInstance("AES").apply { init(KEY_SIZE_BITS) }.generateKey()
        keys[alias] = key
        protections[alias] = protection
        return AliasedKey(alias, key)
    }

    override fun delete(alias: String) {
        keys.remove(alias)
        protections.remove(alias)
    }

    override fun aliases(prefix: String): Set<String> =
        keys.keys.filterTo(mutableSetOf()) { it.startsWith(prefix) }

    private companion object {
        const val KEY_SIZE_BITS = 256
    }
}
