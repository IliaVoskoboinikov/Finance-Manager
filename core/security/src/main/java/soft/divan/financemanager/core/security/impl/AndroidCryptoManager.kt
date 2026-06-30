package soft.divan.financemanager.core.security.impl

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import soft.divan.financemanager.core.security.CryptoManager
import java.security.GeneralSecurityException
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Provider

/**
 * Реализация [CryptoManager] с использованием Android KeyStore System.
 * Использует алгоритм AES/GCM/NoPadding для обеспечения целостности и конфиденциальности.
 */
class AndroidCryptoManager @Inject constructor(
    private val keyStore: KeyStore,
    private val keyGeneratorProvider: Provider<KeyGenerator>,
    @param:Named("AES_CIPHER") private val cipherProvider: Provider<Cipher>
) : CryptoManager {

    private val tag = "AndroidCryptoManager"

    private fun getKey(alias: String): SecretKey {
        return try {
            val entry = keyStore.getEntry(alias, null)
            if (entry is KeyStore.SecretKeyEntry) {
                entry.secretKey
            } else {
                createKey(alias)
            }
        } catch (e: GeneralSecurityException) {
            Log.e(tag, "Security error getting key for alias: $alias, attempting to recreate", e)
            try {
                keyStore.deleteEntry(alias)
            } catch (_: GeneralSecurityException) {
            }
            createKey(alias)
        }
    }

    private fun createKey(alias: String): SecretKey {
        return try {
            keyGeneratorProvider.get().apply {
                init(
                    KeyGenParameterSpec.Builder(
                        alias,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                    )
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setUserAuthenticationRequired(false)
                        .setRandomizedEncryptionRequired(true)
                        .setKeySize(KEY_SIZE_AES)
                        .build()
                )
            }.generateKey()
        } catch (e: GeneralSecurityException) {
            Log.e(tag, "Failed to create key for alias: $alias", e)
            throw e
        }
    }

    override fun encrypt(value: String, alias: String): String {
        return try {
            val cipher = cipherProvider.get()
            cipher.init(Cipher.ENCRYPT_MODE, getKey(alias))
            val encrypted = cipher.doFinal(value.toByteArray(Charsets.UTF_8))
            val iv = cipher.iv

            // Результат: [IV (12 байт)][EncryptedData + AuthTag (16 байт)]
            Base64.encodeToString(iv + encrypted, Base64.DEFAULT)
        } catch (e: GeneralSecurityException) {
            Log.e(tag, "Encryption failed for alias: $alias", e)
            throw e
        }
    }

    override fun decrypt(encryptedValue: String, alias: String): String {
        return try {
            val combined = try {
                Base64.decode(encryptedValue, Base64.DEFAULT)
            } catch (e: IllegalArgumentException) {
                Log.e(tag, "Base64 decoding failed for alias: $alias", e)
                throw GeneralSecurityException("Invalid Base64 input", e)
            }

            if (combined.size < IV_SIZE + GCM_TAG_SIZE) {
                Log.e(tag, "Invalid encrypted data length for alias: $alias. Size: ${combined.size}")
                throw GeneralSecurityException("Invalid encrypted data length")
            }

            val iv = combined.copyOfRange(0, IV_SIZE)
            val encrypted = combined.copyOfRange(IV_SIZE, combined.size)

            val cipher = cipherProvider.get()
            val spec = GCMParameterSpec(GCM_TAG_SIZE_BITS, iv)
            cipher.init(Cipher.DECRYPT_MODE, getKey(alias), spec)

            val decryptedBytes = cipher.doFinal(encrypted)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: GeneralSecurityException) {
            Log.e(tag, "Decryption failed for alias: $alias", e)
            throw e
        }
    }

    companion object {
        private const val IV_SIZE = 12 // Рекомендуемый размер IV для GCM
        private const val GCM_TAG_SIZE = 16 // 128 бит (16 байт)
        private const val KEY_SIZE_AES = 256
        private const val GCM_TAG_SIZE_BITS = GCM_TAG_SIZE * 8
    }
}
