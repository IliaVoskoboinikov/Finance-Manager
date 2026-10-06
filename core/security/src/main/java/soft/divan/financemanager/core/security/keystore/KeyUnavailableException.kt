package soft.divan.financemanager.core.security.keystore

import java.security.GeneralSecurityException

/**
 * Ключом Keystore воспользоваться нельзя: его нет, он инвалидирован системой
 * (`KeyPermanentlyInvalidatedException`) или завёрнутые им данные не проходят проверку целостности.
 *
 * Это не «неверный PIN»: восстановить доступ, повторив попытку, нельзя.
 */
class KeyUnavailableException(message: String, cause: Throwable? = null) :
    GeneralSecurityException(message, cause)
