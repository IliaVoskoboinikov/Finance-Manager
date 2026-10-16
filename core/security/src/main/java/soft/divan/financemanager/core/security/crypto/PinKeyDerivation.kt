package soft.divan.financemanager.core.security.crypto

import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject

/**
 * Ключ AES-256 из PIN: PBKDF2-HMAC-SHA256 с собственной солью.
 *
 * Соль — своя, не та, что у хеша PIN для экрана блокировки: ключ и хеш не должны совпадать ни
 * при каких условиях, иначе хранимый хеш был бы готовым ключом.
 *
 * Сам по себе 4-значный PIN перебирается мгновенно, сколько итераций ни ставь. Стойкость уровню
 * «PIN» даёт не число итераций, а то, что снаружи результат завёрнут неизвлекаемым ключом
 * Keystore: без телефона перебирать нечего. Итерации лишь удорожают перебор на самом устройстве.
 */
class PinKeyDerivation @Inject constructor() {

    /** Ключ из [pin] и [salt]; сам [pin] не затирается — это решает вызывающий код. */
    fun derive(pin: CharArray, salt: ByteArray, iterations: Int): SecretKey {
        val spec = PBEKeySpec(pin, salt, iterations, KEY_SIZE_BITS)
        return try {
            val bytes = SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).encoded
            // SecretKeySpec копирует байты — исходный массив можно затереть сразу
            SecretKeySpec(bytes, KEY_ALGORITHM).also { bytes.fill(0) }
        } finally {
            spec.clearPassword()
        }
    }

    companion object {
        private const val ALGORITHM = "PBKDF2WithHmacSHA256"
        private const val KEY_ALGORITHM = "AES"
        private const val KEY_SIZE_BITS = 256

        /** Длина соли в байтах. */
        const val SALT_SIZE = 16

        /**
         * Итерации для новых ключей — калибровка по замеру, цель ~0,5 с на разблокировку.
         *
         * PBKDF2 на Android считает BouncyCastle (чистая Java): на эмуляторе API 36 (arm64)
         * 310 000 итераций — 3,7 с, 100 000 — 1,1 с, 50 000 — 0,58 с. Рекомендация OWASP в сотни
         * тысяч рассчитана на хеш, который защищает сам себя; здесь снаружи неизвлекаемый ключ
         * Keystore, и итерации лишь удорожают перебор на уже взломанном (root) устройстве.
         *
         * Хранятся рядом с завёрнутым ключом, поэтому число можно поднять позже без миграции:
         * старые ключи разворачиваются со своим значением.
         */
        const val DEFAULT_ITERATIONS = 50_000
    }
}
