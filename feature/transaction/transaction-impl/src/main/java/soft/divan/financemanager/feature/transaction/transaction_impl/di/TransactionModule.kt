package soft.divan.financemanager.feature.transaction.transaction_impl.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import soft.divan.financemanager.feature.transaction.transaction_api.TransactionFeatureApi
import soft.divan.financemanager.feature.transaction.transaction_impl.data.api.TransactionApiService
import soft.divan.financemanager.feature.transaction.transaction_impl.data.datasourse.TransactionLocalDataSource
import soft.divan.financemanager.feature.transaction.transaction_impl.data.datasourse.TransactionRemoteDataSource
import soft.divan.financemanager.feature.transaction.transaction_impl.data.datasourse.impl.TransactionLocalDataSourceImpl
import soft.divan.financemanager.feature.transaction.transaction_impl.data.datasourse.impl.TransactionRemoteDataSourceImpl
import soft.divan.financemanager.feature.transaction.transaction_impl.data.repository.TransactionRepositoryImpl
import soft.divan.financemanager.feature.transaction.transaction_impl.domain.repository.TransactionRepository
import soft.divan.financemanager.feature.transaction.transaction_impl.domain.usecase.CreateTransactionUseCase
import soft.divan.financemanager.feature.transaction.transaction_impl.domain.usecase.GetCategoriesExpensesUseCase
import soft.divan.financemanager.feature.transaction.transaction_impl.domain.usecase.GetTransactionUseCase
import soft.divan.financemanager.feature.transaction.transaction_impl.domain.usecase.impl.CreateTransactionUseCaseImpl
import soft.divan.financemanager.feature.transaction.transaction_impl.domain.usecase.impl.GetCategoriesExpensesUseCaseImpl
import soft.divan.financemanager.feature.transaction.transaction_impl.domain.usecase.impl.GetTransactionUseCaseImpl
import soft.divan.financemanager.feature.transaction.transaction_impl.navigation.TransactionFeatureImpl

@Module
@InstallIn(SingletonComponent::class)
interface TransactionModule {
    @Binds
    fun bindTransactionRouter(impl: TransactionFeatureImpl): TransactionFeatureApi

    @Binds
    fun bindCreateTransactionUseCase(
        impl: CreateTransactionUseCaseImpl
    ): CreateTransactionUseCase

    @Binds
    abstract fun bindGetTransactionUseCase(
        impl: GetTransactionUseCaseImpl
    ): GetTransactionUseCase

    @Binds
    abstract fun bindTransactionRemoteDataSource(
        impl: TransactionRemoteDataSourceImpl
    ): TransactionRemoteDataSource

    @Binds
    abstract fun bindTransactionLocalDataSource(
        impl: TransactionLocalDataSourceImpl
    ): TransactionLocalDataSource

    @Binds
    abstract fun bindTransactionRepository(
        impl: TransactionRepositoryImpl
    ): TransactionRepository


    @Binds
    abstract fun bindGetCategoriesExpensesUseCase(
        impl: GetCategoriesExpensesUseCaseImpl
    ): GetCategoriesExpensesUseCase
}

@Module
@InstallIn(SingletonComponent::class)
object TranModule {
    @Provides
    fun provideTransactionApi(retrofit: Retrofit): TransactionApiService =
        retrofit.create(TransactionApiService::class.java)

}

