package soft.divan.financemanager.core.security.keyset

import soft.divan.financemanager.core.security.crypto.AesGcm
import soft.divan.financemanager.core.security.keystore.KeyProtection
import soft.divan.financemanager.core.security.keystore.KeyUnavailableException
import soft.divan.financemanager.core.security.keystore.KeystoreKeys
import java.security.GeneralSecurityException
import javax.crypto.Cipher
import javax.inject.Inject

/**
 * Биометрическая копия ключа базы на уровне PIN.
 *
 * Ключ Keystore требует строгой биометрии на **каждую** операцию, поэтому шифр готовится до
 * аутентификации, уходит в `BiometricPrompt.CryptoObject` и используется только после неё.
 * Своими силами разблокировать такой шифр нельзя — биометрия здесь действительно выдаёт ключ,
 * а не просто разрешает показать экран.
 *
 * Появился новый отпечаток — Keystore инвалидирует ключ, и [unlockCipher] бросает
 * [KeyUnavailableException]. Это не повод стирать данные: копия под PIN по-прежнему цела.
 */
class BiometricEnvelope @Inject constructor(private val keys: KeystoreKeys) {

    /** Шифр для заворачивания DEK новым биометрическим ключом. */
    fun enrollmentCipher(): BiometricEnrollment {
        val key = keys.create(DekEnvelope.BIOMETRIC_PREFIX, KeyProtection.BIOMETRIC)
        return try {
            BiometricEnrollment(key.alias, AesGcm.encryptCipher(key.key))
        } catch (e: GeneralSecurityException) {
            keys.delete(key.alias)
            throw KeyUnavailableException("Biometric key cannot be used", e)
        }
    }

    /** Завершает заворачивание: [enrollment] уже прошёл биометрическую аутентификацию. */
    fun finishEnrollment(enrollment: BiometricEnrollment, dek: ByteArray): SealedKey =
        SealedKey(enrollment.alias, AesGcm.finishEncrypt(enrollment.cipher, dek))

    /** Шифр для разворачивания DEK — отдаётся в `CryptoObject` до аутентификации. */
    fun unlockCipher(keyset: Keyset): Cipher {
        val target = keys.unsealable(keyset.biometric, "Biometric copy")
        return try {
            AesGcm.decryptCipher(target.key, target.sealed.blob)
        } catch (e: GeneralSecurityException) {
            throw KeyUnavailableException("Biometric key ${target.sealed.alias} is invalidated", e)
        }
    }

    /** DEK из биометрической копии: [cipher] уже прошёл аутентификацию. */
    fun finishUnlock(cipher: Cipher, keyset: Keyset): ByteArray {
        val sealed = keyset.biometric ?: throw KeyUnavailableException("No biometric copy")
        return try {
            AesGcm.finishDecrypt(cipher, sealed.blob)
        } catch (e: GeneralSecurityException) {
            throw KeyUnavailableException("Biometric copy cannot be unsealed", e)
        }
    }
}

/** Незавершённое включение биометрии: ключ создан, шифр ждёт аутентификации. */
class BiometricEnrollment(val alias: String, val cipher: Cipher)
