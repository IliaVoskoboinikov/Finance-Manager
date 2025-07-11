package soft.divan.finansemanager.account.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import soft.divan.financemanager.core.domain.usecase.GetSumTransactionsUseCase
import soft.divan.financemanager.core.domain.usecase.GetSumTransactionsUseCaseImpl
import soft.divan.finansemanager.account.domain.usecase.GetAccountsUseCase
import soft.divan.finansemanager.account.domain.usecase.UpdateAccountUseCase
import soft.divan.finansemanager.account.domain.usecase.UpdateCurrencyUseCase
import soft.divan.finansemanager.account.domain.usecase.impl.GetAccountsUseCaseImpl
import soft.divan.finansemanager.account.domain.usecase.impl.UpdateAccountUseCaseImpl
import soft.divan.finansemanager.account.domain.usecase.impl.UpdateCurrencyUseCaseIml


@Module
@InstallIn(SingletonComponent::class)
abstract class AccountModule {

    @Binds
    abstract fun bindGetAccountsUseCase(
        impl: GetAccountsUseCaseImpl
    ): GetAccountsUseCase

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