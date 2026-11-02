package soft.divan.financemanager.core.security.keyset

/** Чем закрыт ключ базы (DEK). */
enum class KeyLevel {
    /** DEK хранится незавёрнутым: файл базы зашифрован, но ключ лежит рядом. */
    OPEN,

    /** DEK завёрнут неизвлекаемым ключом Keystore; база открывается без участия пользователя. */
    DEVICE,

    /** DEK завёрнут ключом из PIN, а снаружи — ключом Keystore; без PIN базу не открыть. */
    PIN
}

/**
 * Ключ базы, завёрнутый ключом Keystore [alias].
 *
 * Алиас хранится рядом с блобом, а не выводится из уровня: каждое заворачивание получает новый
 * алиас, и старый удаляется только после сохранения нового набора.
 */
class SealedKey(val alias: String, val blob: ByteArray)

/**
 * Сохранённое состояние ключей базы — всё, что нужно, чтобы развернуть DEK на текущем уровне.
 *
 * @property openKey DEK как есть — только на [KeyLevel.OPEN].
 * @property sealed Завёрнутый DEK на [KeyLevel.DEVICE]; на [KeyLevel.PIN] — внешний слой
 *   (Keystore) поверх внутреннего (ключ из PIN).
 * @property pinSalt Соль PBKDF2 для внутреннего слоя [KeyLevel.PIN].
 * @property pinIterations Итерации PBKDF2, с которыми завёрнут внутренний слой.
 * @property biometric Копия DEK под биометрическим ключом — только на [KeyLevel.PIN].
 */
class Keyset(
    val level: KeyLevel,
    val openKey: ByteArray? = null,
    val sealed: SealedKey? = null,
    val pinSalt: ByteArray? = null,
    val pinIterations: Int = 0,
    val biometric: SealedKey? = null
) {

    /** Алиасы Keystore, на которые ссылается набор: всё остальное с нашим префиксом — мусор. */
    fun aliases(): Set<String> = setOfNotNull(sealed?.alias, biometric?.alias)

    /** Тот же набор без биометрической копии. */
    fun withoutBiometric(): Keyset = Keyset(level, openKey, sealed, pinSalt, pinIterations, null)

    /** Тот же набор с биометрической копией [biometric] вместо прежней. */
    fun withBiometric(biometric: SealedKey): Keyset =
        Keyset(level, openKey, sealed, pinSalt, pinIterations, biometric)
}

/**
 * Учёт неверных PIN на уровне [KeyLevel.PIN].
 *
 * Пауза хранится не моментом по часам системы, а длительностью и точкой отсчёта на
 * [MonotonicClock]: часы системы переводятся, монотонные — нет.
 *
 * @property failures Сколько неверных попыток подряд.
 * @property lockoutMillis Длительность паузы после последней ошибки (0 — паузы нет).
 * @property lockoutStartedAtElapsed Показание [MonotonicClock] в момент последней ошибки.
 */
data class PinAttempts(
    val failures: Int = 0,
    val lockoutMillis: Long = 0,
    val lockoutStartedAtElapsed: Long = 0
)
