package soft.divan.financemanager.feature.transaction.impl.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import soft.divan.financemanager.core.domain.usecase.GetAccountByIdUseCase
import soft.divan.financemanager.core.domain.usecase.impl.GetAccountByIdUseCaseImpl
import soft.divan.financemanager.feature.transaction.api.TransactionFeatureApi
import soft.divan.financemanager.feature.transaction.impl.domain.usecase.CreateTransactionAndUpdateAccountUseCase
import soft.divan.financemanager.feature.transaction.impl.domain.usecase.DeleteTransactionAndUpdateAccountUseCase
import soft.divan.financemanager.feature.transaction.impl.domain.usecase.GetCategoriesByTypeUseCase
import soft.divan.financemanager.feature.transaction.impl.domain.usecase.GetTransactionUseCase
import soft.divan.financemanager.feature.transaction.impl.domain.usecase.UpdateTransactionAndUpdateAccountUseCase
import soft.divan.financemanager.feature.transaction.impl.domain.usecase.impl.CreateTransactionAndUpdateAccountUseCaseImpl
import soft.divan.financemanager.feature.transaction.impl.domain.usecase.impl.DeleteTransactionAndUpdateAccountUseCaseImpl
import soft.divan.financemanager.feature.transaction.impl.domain.usecase.impl.GetCategoriesByTypeUseCaseImpl
import soft.divan.financemanager.feature.transaction.impl.domain.usecase.impl.GetTransactionUseCaseImpl
import soft.divan.financemanager.feature.transaction.impl.domain.usecase.impl.UpdateTransactionAndUpdateAccountUseCaseImpl
import soft.divan.financemanager.feature.transaction.impl.navigation.TransactionFeatureImpl

@Module
@InstallIn(SingletonComponent::class)
interface TransactionBinderModule {

    @Binds
    fun bindTransactionRouter(impl: TransactionFeatureImpl): TransactionFeatureApi

    @Binds
    fun bindCreateTransactionUseCase(
        impl: CreateTransactionAndUpdateAccountUseCaseImpl
    ): CreateTransactionAndUpdateAccountUseCase

    @Binds
    fun bindGetTransactionUseCase(impl: GetTransactionUseCaseImpl): GetTransactionUseCase

    @Binds
    fun bindGetCategoriesByTypeUseCase(
        impl: GetCategoriesByTypeUseCaseImpl
    ): GetCategoriesByTypeUseCase

    @Binds
    fun bindDeleteTransactionUseCase(
        impl: DeleteTransactionAndUpdateAccountUseCaseImpl
    ): DeleteTransactionAndUpdateAccountUseCase

    @Binds
    fun bindUpdateTransactionUseCase(
        impl: UpdateTransactionAndUpdateAccountUseCaseImpl
    ): UpdateTransactionAndUpdateAccountUseCase

    @Binds
    fun bindGetAccountByIdUseCase(impl: GetAccountByIdUseCaseImpl): GetAccountByIdUseCase
}
