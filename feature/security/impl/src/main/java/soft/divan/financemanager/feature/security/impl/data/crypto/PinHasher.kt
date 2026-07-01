package soft.divan.financemanager.feature.security.impl.data.crypto

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Хеширование PIN-кода (PBKDF2 + случайная соль) для хранения и сравнения без plaintext.
 *
 * Сырой PIN не сохраняется и не поднимается в presentation: сохраняется только хеш,
 * а проверка идёт сравнением хешей. Формат хранимого значения: `<base64(salt)>:<base64(hash)>`.
 */
object PinHasher {

    private const val ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val ITERATIONS = 100_000
    private const val KEY_LENGTH_BITS = 256
    private const val SALT_LENGTH_BYTES = 16
    private const val SEPARATOR = ":"

    fun hash(pin: String): String {
        val salt = ByteArray(SALT_LENGTH_BYTES).also { SecureRandom().nextBytes(it) }
        val hash = pbkdf2(pin, salt)
        return encode(salt) + SEPARATOR + encode(hash)
    }

    fun verify(pin: String, stored: String): Boolean = runCatching {
        val parts = stored.split(SEPARATOR)
        if (parts.size != 2) return@runCatching false
        val salt = decode(parts[0])
        val expected = decode(parts[1])
        // Сравнение за постоянное время, чтобы результат не утекал по таймингу.
        MessageDigest.isEqual(expected, pbkdf2(pin, salt))
    }.getOrDefault(false)

    private fun pbkdf2(pin: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(pin.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS)
        return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).encoded
    }

    private fun encode(bytes: ByteArray): String = Base64.encodeToString(bytes, Base64.NO_WRAP)

    private fun decode(value: String): ByteArray = Base64.decode(value, Base64.NO_WRAP)
}
