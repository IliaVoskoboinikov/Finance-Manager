package soft.divan.finansemanager.core.database.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import soft.divan.finansemanager.core.database.dao.CategoryDao
import soft.divan.finansemanager.core.database.dao.TransactionDao
import soft.divan.finansemanager.core.database.db.FinanceManagerDatabase


@Module
@InstallIn(SingletonComponent::class)
object LocalDBModule {

    @Provides
    fun provideDatabase(
        @ApplicationContext context: Context
    ): FinanceManagerDatabase =
        Room.databaseBuilder(context, FinanceManagerDatabase::class.java, "finance_manager_db.db")
            .fallbackToDestructiveMigration(false)
            .build()

    @Provides
    fun provideTransactionDao(
        financeManagerDatabase: FinanceManagerDatabase
    ): TransactionDao = financeManagerDatabase.transactionDao()

    @Provides
    fun provideCategoryDaoDao(
        financeManagerDatabase: FinanceManagerDatabase
    ): CategoryDao = financeManagerDatabase.categoryDaoDao()
}