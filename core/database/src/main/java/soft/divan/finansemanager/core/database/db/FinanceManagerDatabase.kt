package soft.divan.finansemanager.core.database.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import soft.divan.finansemanager.core.database.Converters
import soft.divan.finansemanager.core.database.dao.AccountDao
import soft.divan.finansemanager.core.database.dao.CategoryDao
import soft.divan.finansemanager.core.database.dao.TransactionDao
import soft.divan.finansemanager.core.database.entity.AccountEntity
import soft.divan.finansemanager.core.database.entity.CategoryEntity
import soft.divan.finansemanager.core.database.entity.TransactionEntity


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