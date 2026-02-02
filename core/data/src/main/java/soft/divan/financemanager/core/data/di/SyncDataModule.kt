package soft.divan.financemanager.core.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import soft.divan.financemanager.core.data.sync.AccountSyncManager
import soft.divan.financemanager.core.data.sync.CategorySyncManager
import soft.divan.financemanager.core.data.sync.TransactionSyncManager
import soft.divan.financemanager.core.data.sync.impl.AccountSyncManagerImpl
import soft.divan.financemanager.core.data.sync.impl.CategorySyncManagerImpl
import soft.divan.financemanager.core.data.sync.impl.TransactionSyncManagerImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface SyncDataModule {

    @Binds
    @Singleton
    fun bindAccountSyncManager(
        impl: AccountSyncManagerImpl
    ): AccountSyncManager

    @Binds
    @Singleton
    fun bindCategorySyncManager(
        impl: CategorySyncManagerImpl
    ): CategorySyncManager

    @Binds
    @Singleton
    fun bindTransactionSyncManager(
        impl: TransactionSyncManagerImpl
    ): TransactionSyncManager
}