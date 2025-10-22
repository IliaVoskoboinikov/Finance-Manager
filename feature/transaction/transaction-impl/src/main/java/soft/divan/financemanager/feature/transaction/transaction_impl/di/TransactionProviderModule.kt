package soft.divan.financemanager.feature.transaction.transaction_impl.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import soft.divan.financemanager.feature.transaction.transaction_impl.data.api.TransactionApiService

@Module
@InstallIn(SingletonComponent::class)
object TransactionProviderModule {

    @Provides
    fun provideTransactionApi(retrofit: Retrofit): TransactionApiService =
        retrofit.create(TransactionApiService::class.java)

}