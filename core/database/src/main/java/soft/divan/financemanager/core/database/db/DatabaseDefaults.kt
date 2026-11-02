package soft.divan.financemanager.core.database.db

import soft.divan.financemanager.core.database.holder.DatabaseFiles
import androidx.room.Room
import android.content.Context
import androidx.room.RoomDatabase
import soft.divan.financemanager.core.database.seed.SeedDatabaseCallback

/** Сборщик базы приложения. */
typealias DatabaseBuilder = RoomDatabase.Builder<FinanceManagerDatabase>

/**
 * Общая настройка сборщика базы — для рабочей базы (SQLCipher) и тестовых баз, которые её
 * имитируют. Чистым in-memory базам для тестов DAO она не нужна: им нужны пустые таблицы.
 *
 * - засев справочника категорий: при создании файла и после деструктивного отката
 *   ([SeedDatabaseCallback]);
 * - до релиза — деструктивный откат при смене версии: реальных пользователей нет, данные
 *   залогиненного вернутся с сервера. Настоящие миграции с тестами — до релиза (FM-107).
 */
fun DatabaseBuilder.withAppDefaults(): DatabaseBuilder = apply {
    addCallback(SeedDatabaseCallback())
    fallbackToDestructiveMigration(dropAllTables = true)
}

/**
 * Сборщик файла базы приложения: имя файла и [withAppDefaults]. Рабочая фабрика добавляет к нему
 * только шифрование, тестовые берут его как есть — так рецепт рабочей базы один, и тесты на
 * обычном SQLite проверяют его целиком.
 */
fun appDatabaseBuilder(context: Context): DatabaseBuilder =
    Room.databaseBuilder(context, FinanceManagerDatabase::class.java, DatabaseFiles.NAME)
        .withAppDefaults()
