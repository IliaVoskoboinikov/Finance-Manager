package soft.divan.financemanager.core.database.holder

import soft.divan.financemanager.core.database.db.appDatabaseBuilder
import android.content.Context
import androidx.room.Room
import dagger.hilt.android.qualifiers.ApplicationContext
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import soft.divan.financemanager.core.database.db.FinanceManagerDatabase
import soft.divan.financemanager.core.database.db.withAppDefaults
import javax.inject.Inject

/**
 * Открывает базу через SQLCipher: файл всегда зашифрован, ключ — готовый ([RawKey]).
 *
 * Тонкий адаптер над нативной библиотекой: юнит-тестами его не проверить (в JVM `.so` не
 * загрузится), поэтому вся логика — в [RoomDatabaseHolder] и хранилище ключей, а этот класс
 * проверяется инструментальным тестом на эмуляторе.
 */
class SqlCipherDatabaseFactory @Inject constructor(
    @param:ApplicationContext private val context: Context
) : DatabaseFactory {

    override fun open(key: ByteArray): OpenedDatabase {
        check(nativeLibraryLoaded)
        val passphrase = try {
            RawKey.passphrase(key)
        } finally {
            // Дальше ключ живёт только в виде passphrase — её SQLCipher и держит до закрытия
            key.fill(0)
        }
        val database = appDatabaseBuilder(context)
            .openHelperFactory(SupportOpenHelperFactory(passphrase))
            .build()
        var verified = false
        try {
            // Room открывает файл лениво; неверный ключ должен всплыть здесь, а не в первом запросе
            database.openHelper.writableDatabase
            verified = true
        } finally {
            if (!verified) {
                database.close()
                passphrase.fill(0)
            }
        }
        return OpenedDatabase(database) { passphrase.fill(0) }
    }

    private companion object {
        /** Нативная часть SQLCipher; загружается один раз на процесс, до первого открытия. */
        val nativeLibraryLoaded: Boolean by lazy {
            try {
                System.loadLibrary("sqlcipher")
            } catch (e: UnsatisfiedLinkError) {
                // Error прошёл бы мимо обработки сбоев открытия, и данные зависли бы в
                // Initializing; как исключение он становится «данные не открыть»
                throw IllegalStateException("SQLCipher native library is missing", e)
            }
            true
        }
    }
}
