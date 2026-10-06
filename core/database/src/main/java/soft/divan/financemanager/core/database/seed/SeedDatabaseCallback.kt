package soft.divan.financemanager.core.database.seed

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Засевает справочники в только что созданную базу.
 *
 * `onCreate` вызывается ровно один раз — при создании файла, внутри той же транзакции, что и
 * создание таблиц. Поэтому засев не дублируется при повторных открытиях, а после стирания базы
 * (выход из аккаунта, восстановление) срабатывает снова и справочник возвращается.
 *
 * Вставка идёт сырым SQL: DAO на этом этапе недоступны — Room ещё не закончил открытие.
 */
internal class SeedDatabaseCallback : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        DefaultCategories.all.forEach { category ->
            db.execSQL(
                INSERT_CATEGORY,
                arrayOf<Any>(
                    category.id,
                    category.createdAt,
                    category.updatedAt,
                    category.name,
                    category.emoji,
                    if (category.isIncome) SQL_TRUE else SQL_FALSE
                )
            )
        }
    }

    private companion object {
        const val INSERT_CATEGORY =
            "INSERT OR REPLACE INTO categories (id, createdAt, updatedAt, name, emoji, isIncome) " +
                "VALUES (?, ?, ?, ?, ?, ?)"
        const val SQL_TRUE = 1
        const val SQL_FALSE = 0
    }
}
