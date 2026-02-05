package soft.divan.financemanager.feature.transaction.impl.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import soft.divan.financemanager.feature.transaction.api.TransactionFeatureApi
import soft.divan.financemanager.feature.transaction.impl.domain.usecase.CreateTransactionUseCase
import soft.divan.financemanager.feature.transaction.impl.domain.usecase.DeleteTransactionUseCase
import soft.divan.financemanager.feature.transaction.impl.domain.usecase.GetCategoriesByTypeUseCase
import soft.divan.financemanager.feature.transaction.impl.domain.usecase.GetTransactionUseCase
import soft.divan.financemanager.feature.transaction.impl.domain.usecase.UpdateTransactionUseCase
import soft.divan.financemanager.feature.transaction.impl.domain.usecase.impl.CreateTransactionUseCaseImpl
import soft.divan.financemanager.feature.transaction.impl.domain.usecase.impl.DeleteTransactionUseCaseImpl
import soft.divan.financemanager.feature.transaction.impl.domain.usecase.impl.GetCategoriesByTypeUseCaseImpl
import soft.divan.financemanager.feature.transaction.impl.domain.usecase.impl.GetTransactionUseCaseImpl
import soft.divan.financemanager.feature.transaction.impl.domain.usecase.impl.UpdateTransactionUseCaseImpl
import soft.divan.financemanager.feature.transaction.impl.navigation.TransactionFeatureImpl

@Module
@InstallIn(SingletonComponent::class)
interface TransactionBinderModule {

    @Binds
    fun bindTransactionRouter(impl: TransactionFeatureImpl): TransactionFeatureApi

    @Binds
    fun bindCreateTransactionUseCase(impl: CreateTransactionUseCaseImpl): CreateTransactionUseCase

    @Binds
    fun bindGetTransactionUseCase(impl: GetTransactionUseCaseImpl): GetTransactionUseCase

    @Binds
    fun bindGetCategoriesByTypeUseCase(
        impl: GetCategoriesByTypeUseCaseImpl
    ): GetCategoriesByTypeUseCase

    @Binds
    fun bindDeleteTransactionUseCase(impl: DeleteTransactionUseCaseImpl): DeleteTransactionUseCase

    @Binds
    fun bindUpdateTransactionUseCase(impl: UpdateTransactionUseCaseImpl): UpdateTransactionUseCase
}
