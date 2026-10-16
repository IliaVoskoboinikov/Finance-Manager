package soft.divan.financemanager.core.security.keystore.impl

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import soft.divan.financemanager.core.security.keystore.AliasedKey
import soft.divan.financemanager.core.security.keystore.KeyProtection
import soft.divan.financemanager.core.security.keystore.KeyUnavailableException
import soft.divan.financemanager.core.security.keystore.KeystoreKeys
import java.security.GeneralSecurityException
import java.security.KeyStore
import java.security.ProviderException
import java.util.UUID
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.inject.Inject

/**
 * [KeystoreKeys] поверх Android Keystore: AES-256-GCM, случайный IV задаёт сам Keystore.
 *
 * `KeyGenerator` создаётся на каждый вызов, а не берётся общим синглтоном: `init` + `generateKey`
 * на одном экземпляре из двух потоков — гонка, а ключи токенов создаются параллельно.
 *
 * @param newKeyGenerator источник генераторов; подменяется в тестах, где провайдера
 *   `AndroidKeyStore` нет.
 */
class AndroidKeystoreKeys(
    private val keyStore: KeyStore,
    private val newKeyGenerator: () -> KeyGenerator
) : KeystoreKeys {

    @Inject
    constructor(keyStore: KeyStore) : this(keyStore, ::androidKeystoreAesGenerator)

    override fun find(alias: String): SecretKey? = try {
        keyStore.getKey(alias, null) as? SecretKey
    } catch (e: GeneralSecurityException) {
        throw KeyUnavailableException("Keystore key $alias is unreadable", e)
    }

    override fun create(prefix: String, protection: KeyProtection): AliasedKey {
        val alias = "${prefix}_${UUID.randomUUID()}"
        val key = try {
            generate(alias, protection, strict = true)
        } catch (e: GeneralSecurityException) {
            fallbackOrThrow(alias, protection, e)
        } catch (e: ProviderException) {
            fallbackOrThrow(alias, protection, e)
        }
        return AliasedKey(alias, key)
    }

    override fun delete(alias: String) {
        try {
            keyStore.deleteEntry(alias)
        } catch (e: GeneralSecurityException) {
            // Ключ, который не удалось удалить, уберёт уборка осиротевших алиасов при старте.
            Log.w(TAG, "Failed to delete Keystore key $alias", e)
        }
    }

    override fun aliases(prefix: String): Set<String> = try {
        keyStore.aliases().toList().filterTo(mutableSetOf()) { it.startsWith(prefix) }
    } catch (e: GeneralSecurityException) {
        Log.w(TAG, "Failed to list Keystore aliases", e)
        emptySet()
    }

    /**
     * На части устройств ключ с «разблокированным устройством» не создаётся (нет безопасной
     * блокировки экрана, баги прошивки). Тогда внешний слой уровня PIN держится на обычном ключе
     * Keystore — это по-прежнему неизвлекаемый ключ, просто доступный и на заблокированном экране.
     */
    private fun fallbackOrThrow(
        alias: String,
        protection: KeyProtection,
        cause: Exception
    ): SecretKey {
        if (protection != KeyProtection.UNLOCKED_DEVICE) {
            throw KeyUnavailableException("Failed to create Keystore key $alias", cause)
        }
        Log.w(TAG, "Unlocked-device key is not supported, falling back", cause)
        // Отказ Keystore приходит и проверяемым исключением, и ProviderException
        return runCatching { generate(alias, protection, strict = false) }
            .getOrElse { throw KeyUnavailableException("Failed to create Keystore key $alias", it) }
    }

    private fun generate(alias: String, protection: KeyProtection, strict: Boolean): SecretKey {
        val spec = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(true)
            .setKeySize(KEY_SIZE_BITS)
            .applyProtection(protection, strict)
            .build()
        return newKeyGenerator()
            .apply { init(spec) }
            .generateKey()
    }

    private fun KeyGenParameterSpec.Builder.applyProtection(
        protection: KeyProtection,
        strict: Boolean
    ): KeyGenParameterSpec.Builder = apply {
        when (protection) {
            KeyProtection.ALWAYS_AVAILABLE -> setUserAuthenticationRequired(false)

            KeyProtection.UNLOCKED_DEVICE -> {
                setUserAuthenticationRequired(false)
                if (strict && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    setUnlockedDeviceRequired(true)
                }
            }

            KeyProtection.BIOMETRIC -> {
                setUserAuthenticationRequired(true)
                setInvalidatedByBiometricEnrollment(true)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    // 0 секунд — аутентификация нужна на каждую операцию (через CryptoObject)
                    setUserAuthenticationParameters(0, KeyProperties.AUTH_BIOMETRIC_STRONG)
                } else {
                    @Suppress("DEPRECATION")
                    setUserAuthenticationValidityDurationSeconds(-1)
                }
            }
        }
    }

    private companion object {
        const val TAG = "AndroidKeystoreKeys"
        const val KEY_SIZE_BITS = 256
    }
}

private const val ANDROID_KEYSTORE = "AndroidKeyStore"

private fun androidKeystoreAesGenerator(): KeyGenerator =
    KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
