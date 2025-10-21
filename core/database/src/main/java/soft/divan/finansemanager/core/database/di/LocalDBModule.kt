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
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object LocalDBModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): FinanceManagerDatabase =
        Room.databaseBuilder(context, FinanceManagerDatabase::class.java, "finance_manager_db.db")
            .fallbackToDestructiveMigration(false).build()

    @Provides
    @Singleton
    fun provideTransactionDao(db: FinanceManagerDatabase): TransactionDao = db.transactionDao()

    @Provides
    @Singleton
    fun provideCategoryDao(db: FinanceManagerDatabase): CategoryDao = db.categoryDaoDao()

}