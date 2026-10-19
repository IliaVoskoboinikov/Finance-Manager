package soft.divan.financemanager.core.security.keyset

import soft.divan.financemanager.core.security.crypto.AesGcm
import soft.divan.financemanager.core.security.crypto.PinKeyDerivation
import soft.divan.financemanager.core.security.keystore.KeyProtection
import soft.divan.financemanager.core.security.keystore.KeyUnavailableException
import soft.divan.financemanager.core.security.keystore.KeystoreKeys
import java.security.GeneralSecurityException
import java.security.SecureRandom
import javax.crypto.AEADBadTagException
import javax.inject.Inject

/**
 * Заворачивание и разворачивание ключа базы (DEK) на каждом уровне защиты.
 *
 * ### Порядок слоёв на уровне PIN
 * ```
 * хранится = AES-GCM(ключ Keystore, AES-GCM(ключ из PIN, DEK))
 * ```
 * Keystore — **снаружи**. Будь снаружи слой с PIN, его тег GCM сам сообщал бы, угадан ли PIN, и
 * все 10 000 четырёхзначных PIN перебирались бы по скопированным файлам за секунды, без телефона.
 * А так до слоя с PIN не добраться без неизвлекаемого ключа конкретного устройства, и каждая
 * попытка проходит через счётчик [PinAttemptPolicy].
 *
 * Отсюда же различие ошибок: сбой внешнего слоя — [KeyUnavailableException] (ключ потерян или
 * данные испорчены), сбой внутреннего — неверный PIN.
 *
 * @param pinIterations итерации PBKDF2 для новых наборов уровня PIN; в тестах — меньше.
 */
class DekEnvelope(
    private val keys: KeystoreKeys,
    private val derivation: PinKeyDerivation,
    private val pinIterations: Int
) {

    @Inject
    constructor(keys: KeystoreKeys, derivation: PinKeyDerivation) :
        this(keys, derivation, PinKeyDerivation.DEFAULT_ITERATIONS)

    private val random = SecureRandom()

    /** Новый случайный ключ базы. */
    fun newDek(): ByteArray = ByteArray(DEK_SIZE).also(random::nextBytes)

    /** Уровень OPEN: DEK хранится как есть — файл зашифрован, но ключ лежит рядом. */
    fun sealOpen(dek: ByteArray): Keyset = Keyset(level = KeyLevel.OPEN, openKey = dek.copyOf())

    /** Уровень DEVICE: DEK завёрнут новым ключом Keystore, доступным без участия пользователя. */
    fun sealDevice(dek: ByteArray): Keyset {
        val outer = keys.create(DEVICE_PREFIX, KeyProtection.ALWAYS_AVAILABLE)
        return Keyset(
            level = KeyLevel.DEVICE,
            sealed = SealedKey(outer.alias, AesGcm.encrypt(outer.key, dek))
        )
    }

    /**
     * Уровень PIN. [biometric] переносится как есть: биометрическая копия завёрнута ключом,
     * который от PIN не зависит, поэтому смена PIN её не затрагивает.
     */
    fun sealPin(
        dek: ByteArray,
        pin: CharArray,
        biometric: SealedKey? = null,
        iterations: Int = pinIterations
    ): Keyset {
        val salt = ByteArray(PinKeyDerivation.SALT_SIZE).also(random::nextBytes)
        val inner = AesGcm.encrypt(derivation.derive(pin, salt, iterations), dek)
        return try {
            val outer = keys.create(PIN_PREFIX, KeyProtection.UNLOCKED_DEVICE)
            Keyset(
                level = KeyLevel.PIN,
                sealed = SealedKey(outer.alias, AesGcm.encrypt(outer.key, inner)),
                pinSalt = salt,
                pinIterations = iterations,
                biometric = biometric
            )
        } finally {
            inner.fill(0)
        }
    }

    /** DEK уровней OPEN и DEVICE — без участия пользователя. */
    fun openWithoutUser(keyset: Keyset): ByteArray = when (keyset.level) {
        KeyLevel.OPEN -> keyset.openKey?.copyOf()
            ?: throw KeyUnavailableException("Open key is missing")

        KeyLevel.DEVICE -> unsealOuter(keyset)

        KeyLevel.PIN -> throw IllegalArgumentException("PIN level requires the PIN")
    }

    /**
     * DEK уровня PIN или `null`, если PIN неверный.
     *
     * @throws KeyUnavailableException внешний слой не снимается — повтор с другим PIN не поможет.
     */
    fun openWithPin(keyset: Keyset, pin: CharArray): ByteArray? {
        require(keyset.level == KeyLevel.PIN) { "Keyset is not PIN-protected" }
        val salt = keyset.pinSalt ?: throw KeyUnavailableException("PIN salt is missing")
        val inner = unsealOuter(keyset)
        return try {
            AesGcm.decrypt(derivation.derive(pin, salt, keyset.pinIterations), inner)
        } catch (_: AEADBadTagException) {
            null
        } finally {
            inner.fill(0)
        }
    }

    private fun unsealOuter(keyset: Keyset): ByteArray {
        val target = keys.unsealable(keyset.sealed, "Sealed key")
        return try {
            AesGcm.decrypt(target.key, target.sealed.blob)
        } catch (e: GeneralSecurityException) {
            throw KeyUnavailableException("Keystore key ${target.sealed.alias} cannot unseal", e)
        }
    }

    companion object {
        /** Общий префикс алиасов ключей базы — по нему ищутся осиротевшие ключи. */
        const val KEY_PREFIX = "db_kek_"

        const val DEVICE_PREFIX = "${KEY_PREFIX}device"
        const val PIN_PREFIX = "${KEY_PREFIX}pin"
        const val BIOMETRIC_PREFIX = "${KEY_PREFIX}bio"

        private const val DEK_SIZE = 32
    }
}
