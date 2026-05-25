package soft.divan.financemanager.core.security.impl

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import soft.divan.financemanager.core.security.CryptoManager
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.inject.Inject

/**
 * Реализация [CryptoManager] с использованием Android KeyStore System.
 *
 * Основные особенности:
 * 1. Ключи генерируются и хранятся внутри аппаратного модуля безопасности (TEE или SE),
 *    что делает их недоступными для извлечения даже при наличии root-прав.
 * 2. Используется алгоритм AES в режиме CBC с набивкой PKCS7.
 * 3. Для каждой операции шифрования генерируется уникальный вектор инициализации (IV).
 */

class AndroidCryptoManager @Inject constructor() : CryptoManager {
    // Доступ к системному хранилищу ключей
    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
        load(null)
    }

    /**
     * Получает существующий секретный ключ по его [alias] или создает новый.
     */
    private fun getKey(alias: String): SecretKey {
        val existingKey =
            keyStore.getEntry(alias, null) as? KeyStore.SecretKeyEntry
        return existingKey?.secretKey ?: createKey(alias)
    }

    /**
     * Генерирует новый 256-битный AES ключ в безопасном хранилище.
     */
    private fun createKey(alias: String): SecretKey {
        return KeyGenerator.getInstance(ALGORITHM).apply {
            init(
                KeyGenParameterSpec.Builder(
                    alias,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(BLOCK_MODE)
                    .setEncryptionPaddings(PADDING)
                    .setUserAuthenticationRequired(false)
                    .setRandomizedEncryptionRequired(true)
                    .setKeySize(256)
                    .build()
            )
        }.generateKey()
    }

    /**
     * Подготавливает Cipher для шифрования данных.
     */
    private fun getEncryptCipher(alias: String): Cipher {
        return Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.ENCRYPT_MODE, getKey(alias))
        }
    }

    /**
     * Подготавливает Cipher для расшифровки с использованием конкретного вектора инициализации (IV).
     */
    private fun getDecryptCipherForIv(alias: String, iv: ByteArray): Cipher {
        return Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.DECRYPT_MODE, getKey(alias), IvParameterSpec(iv))
        }
    }

    /**
     * Шифрует [value] и возвращает Base64 строку, содержащую [IV + encryptedData].
     */
    override fun encrypt(value: String, alias: String): String {
        val cipher = getEncryptCipher(alias)
        val encrypted = cipher.doFinal(value.toByteArray())
        val iv = cipher.iv // Вектор инициализации, созданный Cipher автоматически

        // Объединяем IV (16 байт) и зашифрованные данные для хранения в одном поле
        return Base64.encodeToString(iv + encrypted, Base64.DEFAULT)
    }

    /**
     * Расшифровывает данные, извлекая IV из начала переданной строки.
     */
    override fun decrypt(encryptedValue: String, alias: String): String {
        val combined = Base64.decode(encryptedValue, Base64.DEFAULT)

        // Извлекаем IV (первые 16 байт)
        val iv = combined.copyOfRange(0, IV_SIZE)
        // Извлекаем сами зашифрованные данные
        val encrypted = combined.copyOfRange(IV_SIZE, combined.size)

        return String(getDecryptCipherForIv(alias, iv).doFinal(encrypted))
    }

    companion object {
        private const val ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
        private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_CBC
        private const val PADDING = KeyProperties.ENCRYPTION_PADDING_PKCS7
        private const val TRANSFORMATION = "$ALGORITHM/$BLOCK_MODE/$PADDING"
        private const val IV_SIZE = 16 // Размер IV для AES всегда 16 байт
    }
}
