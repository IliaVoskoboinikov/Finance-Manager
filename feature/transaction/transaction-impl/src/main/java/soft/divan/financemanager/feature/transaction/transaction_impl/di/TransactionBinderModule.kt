package soft.divan.financemanager.feature.transaction.transaction_impl.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import soft.divan.financemanager.feature.transaction.transaction_api.TransactionFeatureApi
import soft.divan.financemanager.feature.transaction.transaction_impl.data.datasourse.TransactionLocalDataSource
import soft.divan.financemanager.feature.transaction.transaction_impl.data.datasourse.TransactionRemoteDataSource
import soft.divan.financemanager.feature.transaction.transaction_impl.data.datasourse.impl.TransactionLocalDataSourceImpl
import soft.divan.financemanager.feature.transaction.transaction_impl.data.datasourse.impl.TransactionRemoteDataSourceImpl
import soft.divan.financemanager.feature.transaction.transaction_impl.data.repository.TransactionRepositoryImpl
import soft.divan.financemanager.feature.transaction.transaction_impl.domain.repository.TransactionRepository
import soft.divan.financemanager.feature.transaction.transaction_impl.domain.usecase.CreateTransactionUseCase
import soft.divan.financemanager.feature.transaction.transaction_impl.domain.usecase.DeleteTransactionUseCase
import soft.divan.financemanager.feature.transaction.transaction_impl.domain.usecase.GetCategoriesByTypeUseCase
import soft.divan.financemanager.feature.transaction.transaction_impl.domain.usecase.GetTransactionUseCase
import soft.divan.financemanager.feature.transaction.transaction_impl.domain.usecase.UpdateTransactionUseCase
import soft.divan.financemanager.feature.transaction.transaction_impl.domain.usecase.impl.CreateTransactionUseCaseImpl
import soft.divan.financemanager.feature.transaction.transaction_impl.domain.usecase.impl.DeleteTransactionUseCaseImpl
import soft.divan.financemanager.feature.transaction.transaction_impl.domain.usecase.impl.GetCategoriesByTypeUseCaseImpl
import soft.divan.financemanager.feature.transaction.transaction_impl.domain.usecase.impl.GetTransactionUseCaseImpl
import soft.divan.financemanager.feature.transaction.transaction_impl.domain.usecase.impl.UpdateTransactionUseCaseImpl
import soft.divan.financemanager.feature.transaction.transaction_impl.navigation.TransactionFeatureImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface TransactionBinderModule {

    @Binds
    fun bindTransactionRouter(impl: TransactionFeatureImpl): TransactionFeatureApi

    @Binds
    @Singleton
    fun bindTransactionRemoteDataSource(impl: TransactionRemoteDataSourceImpl): TransactionRemoteDataSource

    @Binds
    @Singleton
    fun bindTransactionLocalDataSource(impl: TransactionLocalDataSourceImpl): TransactionLocalDataSource

    @Binds
    @Singleton
    fun bindTransactionRepository(impl: TransactionRepositoryImpl): TransactionRepository

    @Binds
    fun bindCreateTransactionUseCase(impl: CreateTransactionUseCaseImpl): CreateTransactionUseCase

    @Binds
    fun bindGetTransactionUseCase(impl: GetTransactionUseCaseImpl): GetTransactionUseCase

    @Binds
    fun bindGetCategoriesByTypeUseCase(impl: GetCategoriesByTypeUseCaseImpl): GetCategoriesByTypeUseCase

    @Binds
    fun bindDeleteTransactionUseCase(impl: DeleteTransactionUseCaseImpl): DeleteTransactionUseCase

    @Binds
    fun bindUpdateTransactionUseCase(impl: UpdateTransactionUseCaseImpl): UpdateTransactionUseCase

}
