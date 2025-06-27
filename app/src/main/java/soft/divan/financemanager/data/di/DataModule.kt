package soft.divan.financemanager.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import soft.divan.financemanager.data.network.util.ConnectivityManagerNetworkMonitor
import soft.divan.financemanager.data.network.util.NetworkMonitor
import soft.divan.financemanager.data.repository.AccountRepositoryImpl
import soft.divan.financemanager.data.repository.TransactionRepositoryImp
import soft.divan.financemanager.data.source.AccountRemoteDataSource
import soft.divan.financemanager.data.source.AccountRemoteDataSourceImpl
import soft.divan.financemanager.data.source.TransactionRemoteDataSource
import soft.divan.financemanager.data.source.TransactionRemoteDataSourceImpl
import soft.divan.financemanager.domain.repository.AccountRepository
import soft.divan.financemanager.domain.repository.TransactionRepository

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    fun provideAccountRemoteDataSource(
        impl: AccountRemoteDataSourceImpl
    ): AccountRemoteDataSource {
        return impl
    }

    @Provides
    fun provideAccountRepository(
        impl: AccountRepositoryImpl
    ): AccountRepository {
        return impl
    }


    @Provides
    fun provideTransactionRemoteDataSource(
        impl: TransactionRemoteDataSourceImpl
    ): TransactionRemoteDataSource {
        return impl
    }

    @Provides
    fun provideTransactionRepository(
        impl: TransactionRepositoryImp
    ): TransactionRepository {
        return impl
    }

    @Provides
     fun bindsNetworkMonitor(
        networkMonitor: ConnectivityManagerNetworkMonitor,
    ): NetworkMonitor {
         return networkMonitor
     }

}