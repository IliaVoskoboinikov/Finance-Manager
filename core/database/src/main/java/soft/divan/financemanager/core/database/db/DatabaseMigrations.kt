package soft.divan.financemanager.core.database.db

import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import soft.divan.financemanager.core.database.seed.SeedDatabaseCallback

/**
 * Миграции схемы. Базовая схема — версия 8 (`schemas/.../8.json`); каждая следующая версия
 * добавляет сюда `Migration(n, n + 1)` и тест в `MigrationTest`.
 *
 * Деструктивного отката при повышении версии больше нет: без миграции Room упадёт при открытии,
 * и это заметят тесты, а не пользователи, у которых молча пропали бы данные.
 */
object DatabaseMigrations {
    val ALL: List<Migration> = emptyList()
}

/** Сборщик базы приложения. */
typealias DatabaseBuilder = RoomDatabase.Builder<FinanceManagerDatabase>

/**
 * Общая настройка сборщика базы — для рабочей базы и для тестовых.
 *
 * - засев справочника категорий при создании файла;
 * - все миграции;
 * - при понижении версии база пересоздаётся: такое бывает только при переключении веток у
 *   разработчика, а падать там на открытии бессмысленно.
 */
fun DatabaseBuilder.withAppDefaults(): DatabaseBuilder = apply {
    addCallback(SeedDatabaseCallback())
    DatabaseMigrations.ALL.forEach { addMigrations(it) }
    fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
}
