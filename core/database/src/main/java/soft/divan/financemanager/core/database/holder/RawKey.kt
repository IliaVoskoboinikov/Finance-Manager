package soft.divan.financemanager.core.database.holder

/**
 * Ключ для SQLCipher в формате «готового ключа»: `x'<64 hex-символа>'`.
 *
 * Обычную парольную фразу SQLCipher прогоняет через собственный PBKDF2 на каждом открытии —
 * 482 мс против 40 мс с готовым ключом (замер на Pixel 7). Ключ базы и так 32 случайных байта,
 * растягивать его незачем.
 *
 * Строка собирается сразу в `ByteArray`, минуя `String`: строку нельзя затереть, а массив —
 * можно, что и делается при закрытии базы.
 */
internal object RawKey {

    /** Длина ключа базы в байтах. */
    const val KEY_SIZE = 32

    private const val HEX_DIGITS = "0123456789abcdef"
    private const val NIBBLE_BITS = 4
    private const val NIBBLE_MASK = 0x0F

    /** `x'…'` в ASCII для ключа [key]; [key] не меняется. */
    fun passphrase(key: ByteArray): ByteArray {
        require(key.size == KEY_SIZE) { "Database key must be $KEY_SIZE bytes, got ${key.size}" }
        val result = ByteArray(PREFIX.size + key.size * 2 + 1)
        PREFIX.copyInto(result)
        key.forEachIndexed { index, byte ->
            val position = PREFIX.size + index * 2
            result[position] = hex(byte.toInt() shr NIBBLE_BITS)
            result[position + 1] = hex(byte.toInt())
        }
        result[result.lastIndex] = QUOTE
        return result
    }

    private fun hex(value: Int): Byte = HEX_DIGITS[value and NIBBLE_MASK].code.toByte()

    private const val QUOTE = '\''.code.toByte()
    private val PREFIX = byteArrayOf('x'.code.toByte(), QUOTE)
}
