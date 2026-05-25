package soft.divan.financemanager.core.security

/**
 * Интерфейс для шифрования и дешифрования данных.
 */
interface CryptoManager {
    /**
     * Шифрует строку и возвращает результат в формате Base64.
     * @param value Исходная строка.
     * @param alias Уникальный идентификатор ключа в KeyStore.
     */
    fun encrypt(value: String, alias: String): String

    /**
     * Расшифровывает строку из формата Base64.
     * @param encryptedValue Зашифрованная строка (Base64), содержащая IV и данные.
     * @param alias Уникальный идентификатор ключа.
     */
    fun decrypt(encryptedValue: String, alias: String): String
}
