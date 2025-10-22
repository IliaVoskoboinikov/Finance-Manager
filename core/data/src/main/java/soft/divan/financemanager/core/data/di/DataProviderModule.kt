package soft.divan.financemanager.core.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import soft.divan.financemanager.core.data.api.AccountApiService
import soft.divan.financemanager.core.data.api.CategoryApiService
import soft.divan.financemanager.core.data.api.TransactionApiService
import soft.divan.financemanager.core.data.source.CurrencyLocalDataSource
import soft.divan.financemanager.core.data.source.impl.CurrencyLocalDataSourceImpl
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object DataProviderModule {

    private val Context.dataStore by preferencesDataStore("currency_dataStore")

    @Provides
    @Singleton
    @CurrencyDataStore
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.dataStore

    @Provides
    @Singleton
    fun provideCurrencyLocalDataSource(@CurrencyDataStore dataStore: DataStore<Preferences>): CurrencyLocalDataSource =
        CurrencyLocalDataSourceImpl(dataStore)

    @Provides
    @Singleton
    fun provideAccountApi(retrofit: Retrofit): AccountApiService =
        retrofit.create(AccountApiService::class.java)

    @Provides
    @Singleton
    fun provideTransactionApi(retrofit: Retrofit): TransactionApiService =
        retrofit.create(TransactionApiService::class.java)

    @Provides
    @Singleton
    fun provideCategoryApi(retrofit: Retrofit): CategoryApiService =
        retrofit.create(CategoryApiService::class.java)

}