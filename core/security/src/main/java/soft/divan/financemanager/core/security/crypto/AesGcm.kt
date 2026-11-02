package soft.divan.financemanager.core.security.crypto

import java.security.GeneralSecurityException
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * AES-GCM для коротких секретов: блоб = `IV (12 байт) || шифротекст || тег (16 байт)`.
 *
 * IV генерирует сам шифр: для ключей Keystore с `setRandomizedEncryptionRequired(true)` задать
 * свой IV при шифровании и нельзя. Тег GCM проверяет целостность — поэтому неверный ключ при
 * расшифровке не даёт «мусор», а бросает `AEADBadTagException`. На этом построена проверка PIN.
 *
 * Для биометрии шифр нужно получить **до** аутентификации и отдать в `CryptoObject`, а
 * воспользоваться — после; для этого есть пары `...Cipher` / `finish...`.
 */
object AesGcm {

    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val IV_SIZE = 12
    private const val TAG_SIZE_BITS = 128

    /** Шифрует [plaintext] ключом [key] со случайным IV; результат — блоб. */
    fun encrypt(key: SecretKey, plaintext: ByteArray): ByteArray =
        finishEncrypt(encryptCipher(key), plaintext)

    /** Расшифровывает блоб; неверный ключ или испорченный блоб — `AEADBadTagException`. */
    fun decrypt(key: SecretKey, blob: ByteArray): ByteArray =
        finishDecrypt(decryptCipher(key, blob), blob)

    /** Шифр для шифрования, готовый к [finishEncrypt] (IV уже выбран). */
    fun encryptCipher(key: SecretKey): Cipher =
        Cipher.getInstance(TRANSFORMATION).apply { init(Cipher.ENCRYPT_MODE, key) }

    /** Шифрует [plaintext] подготовленным [cipher] и приклеивает его IV в начало блоба. */
    fun finishEncrypt(cipher: Cipher, plaintext: ByteArray): ByteArray {
        val ciphertext = cipher.doFinal(plaintext)
        return cipher.iv + ciphertext
    }

    /** Шифр для расшифровки [blob], готовый к [finishDecrypt]: IV берётся из блоба. */
    fun decryptCipher(key: SecretKey, blob: ByteArray): Cipher {
        requireValid(blob)
        return Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_SIZE_BITS, blob, 0, IV_SIZE))
        }
    }

    /** Расшифровывает [blob] подготовленным [cipher]. */
    fun finishDecrypt(cipher: Cipher, blob: ByteArray): ByteArray {
        requireValid(blob)
        return cipher.doFinal(blob, IV_SIZE, blob.size - IV_SIZE)
    }

    private fun requireValid(blob: ByteArray) {
        if (blob.size < IV_SIZE + TAG_SIZE_BITS / Byte.SIZE_BITS) {
            throw GeneralSecurityException("Sealed blob is too short: ${blob.size} bytes")
        }
    }
}
