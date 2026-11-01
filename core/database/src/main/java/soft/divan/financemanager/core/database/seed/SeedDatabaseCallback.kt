package soft.divan.financemanager.core.database.seed

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Засевает справочники в только что созданную базу.
 *
 * `onCreate` вызывается ровно один раз — при создании файла, внутри той же транзакции, что и
 * создание таблиц. Поэтому засев не дублируется при повторных открытиях.
 *
 * Пока база собирается с `fallbackToDestructiveMigration`, нужен и повторный засев: при
 * повышении версии Room пересоздаёт таблицы без вызова `onCreate`, и справочник пропал бы до
 * первого синка — а у гостя навсегда. `onDestructiveMigration` приходит сразу после удаления
 * таблиц, но до их создания, поэтому он только ставит флаг, а засев идёт в `onOpen`.
 *
 * Вставка идёт сырым SQL: DAO на этом этапе недоступны — Room ещё не закончил открытие.
 */
internal class SeedDatabaseCallback : RoomDatabase.Callback() {

    /** Таблицы пересозданы деструктивным откатом, справочник надо засеять при открытии. */
    @Volatile
    private var isReseedPending = false

    override fun onCreate(db: SupportSQLiteDatabase) = seed(db)

    override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
        isReseedPending = true
    }

    override fun onOpen(db: SupportSQLiteDatabase) {
        if (!isReseedPending) return
        isReseedPending = false
        seed(db)
    }

    private fun seed(db: SupportSQLiteDatabase) {
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
