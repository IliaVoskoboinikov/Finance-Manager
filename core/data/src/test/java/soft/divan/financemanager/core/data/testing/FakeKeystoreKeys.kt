package soft.divan.financemanager.core.data.testing

import soft.divan.financemanager.core.security.keystore.AliasedKey
import soft.divan.financemanager.core.security.keystore.KeyProtection
import soft.divan.financemanager.core.security.keystore.KeyUnavailableException
import soft.divan.financemanager.core.security.keystore.KeystoreKeys
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

/**
 * Программный заменитель Android Keystore: те же контракты, ключи — обычные AES.
 *
 * Провайдера `AndroidKeyStore` в JVM нет. Биометрия здесь не спрашивается — шифр готов сразу,
 * поэтому тесты проходят оба шага биометрической операции без `BiometricPrompt`.
 */
class FakeKeystoreKeys : KeystoreKeys {

    val keys = linkedMapOf<String, SecretKey>()
    val protections = mutableMapOf<String, KeyProtection>()

    /** Создание ключей падает — как на прошивке без поддержки нужного режима. */
    var failCreate = false

    /** Вызывается при каждом обращении к ключу — чтобы увидеть, что уже сделано к этому моменту. */
    var onFind: (alias: String) -> Unit = {}

    private var counter = 0

    override fun find(alias: String): SecretKey? {
        onFind(alias)
        return keys[alias]
    }

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

    /** Ключ остаётся, но пользоваться им нельзя — как после нового отпечатка в системе. */
    fun invalidate(alias: String) {
        keys[alias] = SecretKeySpec(ByteArray(DES_KEY_SIZE), "DES")
    }

    private companion object {
        const val KEY_SIZE_BITS = 256
        const val DES_KEY_SIZE = 8
    }
}
