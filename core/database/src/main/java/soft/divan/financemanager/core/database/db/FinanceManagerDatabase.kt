package soft.divan.financemanager.core.database.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import soft.divan.financemanager.core.database.dao.AccountDao
import soft.divan.financemanager.core.database.dao.CategoryDao
import soft.divan.financemanager.core.database.dao.TransactionDao
import soft.divan.financemanager.core.database.entity.AccountEntity
import soft.divan.financemanager.core.database.entity.CategoryEntity
import soft.divan.financemanager.core.database.entity.TransactionEntity
import soft.divan.financemanager.core.database.util.Converters

@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class,
        AccountEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class FinanceManagerDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun accountDao(): AccountDao
}
