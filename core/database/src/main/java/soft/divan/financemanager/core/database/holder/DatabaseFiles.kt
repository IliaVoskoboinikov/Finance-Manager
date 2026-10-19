package soft.divan.financemanager.core.database.holder

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.IOException
import javax.inject.Inject

/**
 * Файлы базы на диске: есть ли они, зашифрованы ли, и удаление вместе с журналами.
 */
class DatabaseFiles @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    /** Основной файл базы. */
    val file: File get() = context.getDatabasePath(NAME)

    /** Есть ли файл базы. */
    fun exists(): Boolean = file.exists()

    /**
     * Файл — обычная незашифрованная SQLite-база, оставшаяся от версии приложения без шифрования.
     *
     * Такой файл узнаётся по заголовку `SQLite format 3\0`: у зашифрованного файла первые 16 байт —
     * случайная соль. SQLCipher его не откроет, а реальных пользователей у приложения ещё нет,
     * поэтому его просто удаляют и создают новую базу.
     */
    fun isPlaintext(): Boolean {
        if (!file.exists()) return false
        val header = ByteArray(SQLITE_HEADER.size)
        return try {
            val read = file.inputStream().use { it.read(header) }
            read == header.size && header.contentEquals(SQLITE_HEADER)
        } catch (_: IOException) {
            false
        }
    }

    /** Удаляет базу вместе с журналами (`-wal`, `-shm`, `-journal`). */
    fun delete() {
        context.deleteDatabase(NAME)
    }

    companion object {
        /** Имя файла базы — то же, что было до шифрования. */
        const val NAME = "finance_manager_db.db"

        private val SQLITE_HEADER = "SQLite format 3\u0000".toByteArray(Charsets.US_ASCII)
    }
}
