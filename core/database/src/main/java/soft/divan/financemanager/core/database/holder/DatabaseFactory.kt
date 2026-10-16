package soft.divan.financemanager.core.database.holder

import soft.divan.financemanager.core.database.db.FinanceManagerDatabase

/**
 * Открывает файл базы ключом.
 *
 * Вынесено в зависимость, чтобы [DatabaseHolder] проверялся в JVM-тестах: нативная библиотека
 * SQLCipher в Robolectric не загрузится, и тесты подставляют обычный SQLite.
 */
fun interface DatabaseFactory {

    /**
     * Открывает базу ключом [key] (32 байта) и проверяет его первым обращением к файлу.
     *
     * Массив переходит во владение открытой базы и затирается при её закрытии.
     */
    fun open(key: ByteArray): OpenedDatabase
}

/**
 * Открытая база вместе с действием, которое нужно выполнить после её закрытия (затереть ключ).
 */
class OpenedDatabase(
    val database: FinanceManagerDatabase,
    private val afterClose: () -> Unit = {}
) {

    /** Закрывает базу и выполняет завершающее действие даже при ошибке закрытия. */
    fun close() {
        try {
            database.close()
        } finally {
            afterClose()
        }
    }
}
