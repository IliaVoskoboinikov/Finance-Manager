package soft.divan.financemanager.feature.account.account_impl.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import soft.divan.financemanager.core.domain.usecase.GetSumTransactionsUseCase
import soft.divan.financemanager.core.domain.usecase.GetSumTransactionsUseCaseImpl
import soft.divan.financemanager.feature.account.account_impl.AccountFeatureApi
import soft.divan.financemanager.feature.account.account_impl.domain.usecase.UpdateAccountUseCase
import soft.divan.financemanager.feature.account.account_impl.domain.usecase.UpdateCurrencyUseCase
import soft.divan.financemanager.feature.account.account_impl.domain.usecase.impl.UpdateAccountUseCaseImpl
import soft.divan.financemanager.feature.account.account_impl.domain.usecase.impl.UpdateCurrencyUseCaseIml
import soft.divan.financemanager.feature.account.account_impl.navigation.AccountFeatureImpl


@Module
@InstallIn(SingletonComponent::class)
abstract class AccountModule {

    @Binds
    abstract fun bindAccountRouter(impl: AccountFeatureImpl): AccountFeatureApi


    @Binds
    abstract fun bindUpdateAccountUseCase(
        impl: UpdateAccountUseCaseImpl
    ): UpdateAccountUseCase


    @Binds
    abstract fun bindGetSumTransactionsUseCase(
        impl: GetSumTransactionsUseCaseImpl
    ): GetSumTransactionsUseCase


    @Binds
    abstract fun bindUpdateCurrencyUseCase(
        impl: UpdateCurrencyUseCaseIml
    ): UpdateCurrencyUseCase
}