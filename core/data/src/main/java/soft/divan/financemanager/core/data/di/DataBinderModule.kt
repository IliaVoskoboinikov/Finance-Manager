package soft.divan.financemanager.core.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import soft.divan.financemanager.core.data.repository.AccountRepositoryImpl
import soft.divan.financemanager.core.data.repository.CategoryRepositoryImpl
import soft.divan.financemanager.core.data.repository.CurrencyRepositoryImpl
import soft.divan.financemanager.core.data.repository.TransactionRepositoryImpl
import soft.divan.financemanager.core.data.source.AccountLocalDataSource
import soft.divan.financemanager.core.data.source.AccountRemoteDataSource
import soft.divan.financemanager.core.data.source.CategoryLocalDataSource
import soft.divan.financemanager.core.data.source.CategoryRemoteDataSource
import soft.divan.financemanager.core.data.source.TransactionLocalDataSource
import soft.divan.financemanager.core.data.source.TransactionRemoteDataSource
import soft.divan.financemanager.core.data.source.impl.AccountLocalDataSourceImpl
import soft.divan.financemanager.core.data.source.impl.AccountRemoteDataSourceImpl
import soft.divan.financemanager.core.data.source.impl.CategoryLocalDataSourceImpl
import soft.divan.financemanager.core.data.source.impl.CategoryRemoteDataSourceImpl
import soft.divan.financemanager.core.data.source.impl.TransactionLocalDataSourceImpl
import soft.divan.financemanager.core.data.source.impl.TransactionRemoteDataSourceImpl
import soft.divan.financemanager.core.data.sync.AccountSyncManager
import soft.divan.financemanager.core.data.sync.CategorySyncManager
import soft.divan.financemanager.core.data.sync.TransactionSyncManager
import soft.divan.financemanager.core.data.sync.impl.AccountSyncManagerImpl
import soft.divan.financemanager.core.data.sync.impl.CategorySyncManagerImpl
import soft.divan.financemanager.core.data.sync.impl.TransactionSyncManagerImpl
import soft.divan.financemanager.core.domain.repository.AccountRepository
import soft.divan.financemanager.core.domain.repository.CategoryRepository
import soft.divan.financemanager.core.domain.repository.CurrencyRepository
import soft.divan.financemanager.core.domain.repository.TransactionRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface DataBinderModule {

    @Binds
    @Singleton
    fun bindAccountRemoteDataSource(impl: AccountRemoteDataSourceImpl): AccountRemoteDataSource

    @Binds
    @Singleton
    fun bindAccountLocalDataSource(impl: AccountLocalDataSourceImpl): AccountLocalDataSource

    @Binds
    @Singleton
    fun bindAccountRepository(impl: AccountRepositoryImpl): AccountRepository

    @Binds
    @Singleton
    fun bindTransactionRemoteDataSource(impl: TransactionRemoteDataSourceImpl): TransactionRemoteDataSource

    @Binds
    @Singleton
    fun bindTransactionRepository(impl: TransactionRepositoryImpl): TransactionRepository

    @Binds
    @Singleton
    fun bindTransactionLocalDataSource(impl: TransactionLocalDataSourceImpl): TransactionLocalDataSource

    @Binds
    @Singleton
    fun bindCurrencyRepository(impl: CurrencyRepositoryImpl): CurrencyRepository

    @Binds
    @Singleton
    fun bindCategoryRemoteDataSource(impl: CategoryRemoteDataSourceImpl): CategoryRemoteDataSource

    @Binds
    @Singleton
    fun bindCategoryLocalDataSource(impl: CategoryLocalDataSourceImpl): CategoryLocalDataSource

    @Binds
    @Singleton
    fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository

    @Binds
    @Singleton
    fun bindAccountSyncManager(impl: AccountSyncManagerImpl): AccountSyncManager

    @Binds
    @Singleton
    fun bindCategorySyncManager(impl: CategorySyncManagerImpl): CategorySyncManager

    @Binds
    @Singleton
    fun bindTransactionSyncManager(impl: TransactionSyncManagerImpl): TransactionSyncManager
}
