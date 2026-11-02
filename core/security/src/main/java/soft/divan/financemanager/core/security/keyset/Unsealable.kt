package soft.divan.financemanager.core.security.keyset

import soft.divan.financemanager.core.security.keystore.KeyUnavailableException
import soft.divan.financemanager.core.security.keystore.KeystoreKeys
import javax.crypto.SecretKey

/** Завёрнутый ключ вместе с ключом Keystore, которым его можно развернуть. */
internal class Unsealable(val sealed: SealedKey, val key: SecretKey)

/**
 * Ключ Keystore для [sealed]. Нет завёрнутой копии ([what] — что именно искали) или ключа
 * Keystore под её алиасом — [KeyUnavailableException]: повторная попытка тут не поможет.
 */
internal fun KeystoreKeys.unsealable(sealed: SealedKey?, what: String): Unsealable {
    if (sealed == null) throw KeyUnavailableException("$what is missing")
    val key = find(sealed.alias) ?: throw KeyUnavailableException("Keystore key ${sealed.alias} is gone")
    return Unsealable(sealed, key)
}
