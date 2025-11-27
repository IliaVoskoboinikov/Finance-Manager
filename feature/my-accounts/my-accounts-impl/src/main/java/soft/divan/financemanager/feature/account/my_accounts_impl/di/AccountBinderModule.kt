package soft.divan.financemanager.feature.account.my_accounts_impl.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import soft.divan.financemanager.core.domain.usecase.GetAccountsUseCase
import soft.divan.financemanager.core.domain.usecase.GetSumTransactionsUseCase
import soft.divan.financemanager.core.domain.usecase.impl.GetAccountsUseCaseImpl
import soft.divan.financemanager.core.domain.usecase.impl.GetSumTransactionsUseCaseImpl
import soft.divan.financemanager.feature.account.my_accounts_impl.AccountFeatureApi
import soft.divan.financemanager.feature.account.my_accounts_impl.domain.usecase.UpdateCurrencyUseCase
import soft.divan.financemanager.feature.account.my_accounts_impl.domain.usecase.impl.UpdateCurrencyUseCaseIml
import soft.divan.financemanager.feature.account.my_accounts_impl.navigation.AccountFeatureImpl

@Module
@InstallIn(SingletonComponent::class)
interface AccountBinderModule {

    @Binds
    fun bindAccountRouter(impl: AccountFeatureImpl): AccountFeatureApi

    @Binds
    fun bindGetSumTransactionsUseCase(impl: GetSumTransactionsUseCaseImpl): GetSumTransactionsUseCase

    @Binds
    fun bindUpdateCurrencyUseCase(impl: UpdateCurrencyUseCaseIml): UpdateCurrencyUseCase

    @Binds
    fun bindGetAccountsUseCase(impl: GetAccountsUseCaseImpl): GetAccountsUseCase

}