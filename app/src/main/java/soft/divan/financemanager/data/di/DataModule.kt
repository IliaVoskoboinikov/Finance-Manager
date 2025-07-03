package soft.divan.financemanager.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import soft.divan.financemanager.data.network.util.ConnectivityManagerNetworkMonitor
import soft.divan.financemanager.data.network.util.NetworkMonitor
import soft.divan.financemanager.data.repository.AccountRepositoryImpl
import soft.divan.financemanager.data.repository.CategoryRepositoryImpl
import soft.divan.financemanager.data.repository.TransactionRepositoryImp
import soft.divan.financemanager.data.source.AccountRemoteDataSource
import soft.divan.financemanager.data.source.AccountRemoteDataSourceImpl
import soft.divan.financemanager.data.source.CategoryRemoteDataSource
import soft.divan.financemanager.data.source.CategoryRemoteDataSourceImpl
import soft.divan.financemanager.data.source.TransactionRemoteDataSource
import soft.divan.financemanager.data.source.TransactionRemoteDataSourceImpl
import soft.divan.financemanager.domain.repository.AccountRepository
import soft.divan.financemanager.domain.repository.CategoryRepository
import soft.divan.financemanager.domain.repository.TransactionRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    abstract fun bindAccountRemoteDataSource(
        impl: AccountRemoteDataSourceImpl
    ): AccountRemoteDataSource

    @Binds
    abstract fun bindAccountRepository(
        impl: AccountRepositoryImpl
    ): AccountRepository


    @Binds
    abstract fun bindTransactionRemoteDataSource(
        impl: TransactionRemoteDataSourceImpl
    ): TransactionRemoteDataSource

    @Binds
    abstract fun bindTransactionRepository(
        impl: TransactionRepositoryImp
    ): TransactionRepository

    @Binds
    abstract fun bindNetworkMonitor(
        networkMonitor: ConnectivityManagerNetworkMonitor,
    ): NetworkMonitor


    @Binds
    abstract fun bindCategoryRemoteDataSource(
        impl: CategoryRemoteDataSourceImpl
    ): CategoryRemoteDataSource

    @Binds
    abstract fun bindCategoryRepository(
        impl: CategoryRepositoryImpl
    ): CategoryRepository

}