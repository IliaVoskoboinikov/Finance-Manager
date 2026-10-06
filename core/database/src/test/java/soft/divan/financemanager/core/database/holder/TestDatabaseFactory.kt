package soft.divan.financemanager.core.database.holder

import android.content.Context
import androidx.room.Room
import soft.divan.financemanager.core.database.db.FinanceManagerDatabase
import soft.divan.financemanager.core.database.db.withAppDefaults

/**
 * [DatabaseFactory] на обычном SQLite: нативный SQLCipher в Robolectric не загружается, а всё,
 * что проверяется здесь (аренды, засев, миграции), от шифрования не зависит.
 *
 * Ключ не используется, но принимается во владение так же, как в рабочей фабрике, — чтобы тесты
 * видели, что он затирается при закрытии.
 */
class TestDatabaseFactory(
    private val context: Context,
    private val inMemory: Boolean = false
) : DatabaseFactory {

    /** Ключи, отданные базе, — в порядке открытий. */
    val keys = mutableListOf<ByteArray>()

    /** Следующее открытие упадёт с этим исключением. */
    var failNext: RuntimeException? = null

    /** Закрытие следующей открытой базы упадёт с этим исключением (ключ всё равно затрётся). */
    var failClose: RuntimeException? = null

    override fun open(key: ByteArray): OpenedDatabase {
        failNext?.let { error ->
            failNext = null
            key.fill(0)
            throw error
        }
        val builder = if (inMemory) {
            Room.inMemoryDatabaseBuilder(context, FinanceManagerDatabase::class.java)
        } else {
            Room.databaseBuilder(context, FinanceManagerDatabase::class.java, DatabaseFiles.NAME)
        }
        val database = builder.withAppDefaults().build()
        database.openHelper.writableDatabase
        keys += key
        val closeError = failClose.also { failClose = null }
        return OpenedDatabase(database) {
            key.fill(0)
            closeError?.let { throw it }
        }
    }
}
