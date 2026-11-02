package soft.divan.financemanager.core.security.keystore

import java.security.GeneralSecurityException

/**
 * Ключом Keystore воспользоваться нельзя: его нет, он инвалидирован системой
 * (`KeyPermanentlyInvalidatedException`) или завёрнутые им данные не проходят проверку целостности.
 *
 * Это не «неверный PIN»: восстановить доступ, повторив попытку, нельзя.
 */
open class KeyUnavailableException(message: String, cause: Throwable? = null) :
    GeneralSecurityException(message, cause)

/**
 * Хранилище Keystore не удалось прочитать. В отличие от потерянного или инвалидированного ключа,
 * сбой бывает временным, поэтому завёрнутые копии из-за него не удаляют.
 */
class KeystoreUnreadableException(alias: String, cause: Throwable) :
    KeyUnavailableException("Keystore key $alias is unreadable", cause)
