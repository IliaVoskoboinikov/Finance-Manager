package soft.divan.financemanager.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import soft.divan.core.currency.repository.CurrencyRepository
import soft.divan.financemanager.category.data.repository.CategoryRepositoryImpl
import soft.divan.financemanager.category.data.source.CategoryRemoteDataSource
import soft.divan.financemanager.category.data.source.CategoryRemoteDataSourceImpl
import soft.divan.financemanager.category.domain.repository.CategoryRepository
import soft.divan.financemanager.core.network.util.ConnectivityManagerNetworkMonitor
import soft.divan.financemanager.core.network.util.NetworkMonitor
import soft.divan.financemanager.data.repository.CurrencyRepositoryImpl
import soft.divan.financemanager.data.repository.TransactionRepositoryImp
import soft.divan.financemanager.data.source.CurrencyLocalDataSource
import soft.divan.financemanager.data.source.CurrencyLocalDataSourceImpl
import soft.divan.financemanager.data.source.TransactionRemoteDataSource
import soft.divan.financemanager.data.source.TransactionRemoteDataSourceImpl
import soft.divan.financemanager.domain.repository.TransactionRepository
import soft.divan.finansemanager.account.data.repository.AccountRepositoryImpl
import soft.divan.finansemanager.account.data.sourse.AccountRemoteDataSource
import soft.divan.finansemanager.account.data.sourse.AccountRemoteDataSourceImpl
import soft.divan.finansemanager.account.domain.repository.AccountRepository

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

    @Binds
    abstract fun bindCurrencyRepository(
        impl: CurrencyRepositoryImpl
    ): CurrencyRepository


}

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    private val Context.dataStore by preferencesDataStore("currency_dataStore")

    @Provides
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }

    @Provides
    fun provideCurrencyLocalDataSource(
        dataStore: DataStore<Preferences>
    ): CurrencyLocalDataSource {
        return CurrencyLocalDataSourceImpl(dataStore)
    }
}