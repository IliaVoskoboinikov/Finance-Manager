package soft.divan.financemanager.core.data.di

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
import retrofit2.Retrofit
import soft.divan.financemanager.core.data.api.AccountApiService
import soft.divan.financemanager.core.data.api.CategoryApiService
import soft.divan.financemanager.core.data.api.TransactionApiService
import soft.divan.financemanager.core.data.repository.AccountRepositoryImpl
import soft.divan.financemanager.core.data.repository.CategoryRepositoryImpl
import soft.divan.financemanager.core.data.repository.CurrencyRepositoryImpl
import soft.divan.financemanager.core.data.repository.TransactionRepositoryImp
import soft.divan.financemanager.core.data.source.AccountRemoteDataSource
import soft.divan.financemanager.core.data.source.CategoryRemoteDataSource
import soft.divan.financemanager.core.data.source.CategoryRemoteDataSourceImpl
import soft.divan.financemanager.core.data.source.CurrencyLocalDataSource
import soft.divan.financemanager.core.data.source.TransactionRemoteDataSource
import soft.divan.financemanager.core.data.source.impl.AccountRemoteDataSourceImpl
import soft.divan.financemanager.core.data.source.impl.CurrencyLocalDataSourceImpl
import soft.divan.financemanager.core.data.source.impl.TransactionRemoteDataSourceImpl
import soft.divan.financemanager.core.domain.repository.AccountRepository
import soft.divan.financemanager.core.domain.repository.CategoryRepository
import soft.divan.financemanager.core.domain.repository.CurrencyRepository
import soft.divan.financemanager.core.domain.repository.TransactionRepository
import soft.divan.financemanager.core.domain.usecase.GetAccountsUseCase
import soft.divan.financemanager.core.domain.usecase.GetAccountsUseCaseImpl
import soft.divan.financemanager.core.domain.usecase.GetCategoriesUseCase
import soft.divan.financemanager.core.domain.usecase.GetCategoriesUseCaseImpl

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
    abstract fun bindCurrencyRepository(
        impl: CurrencyRepositoryImpl
    ): CurrencyRepository

    @Binds
    abstract fun bindCategoryRemoteDataSource(
        impl: CategoryRemoteDataSourceImpl
    ): CategoryRemoteDataSource

    @Binds
    abstract fun bindGetCategoriesUseCase(
        impl: GetCategoriesUseCaseImpl
    ): GetCategoriesUseCase

    @Binds
    abstract fun bindCategoryRepository(
        impl: CategoryRepositoryImpl
    ): CategoryRepository

    @Binds
    abstract fun bindGetAccountsUseCase(
        impl: GetAccountsUseCaseImpl
    ): GetAccountsUseCase
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

    @Provides
    fun provideAccountApi(retrofit: Retrofit): AccountApiService =
        retrofit.create(AccountApiService::class.java)


    @Provides
    fun provideTransactionApi(retrofit: Retrofit): TransactionApiService =
        retrofit.create(TransactionApiService::class.java)

    @Provides
    fun provideCategoryApi(retrofit: Retrofit): CategoryApiService =
        retrofit.create(CategoryApiService::class.java)


}