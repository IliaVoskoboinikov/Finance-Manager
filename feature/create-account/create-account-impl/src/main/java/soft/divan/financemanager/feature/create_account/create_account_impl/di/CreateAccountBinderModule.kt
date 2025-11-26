package soft.divan.financemanager.feature.create_account.create_account_impl.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import soft.divan.financemanager.feature.create_account.create_account_api.CreateAccountFeatureApi
import soft.divan.financemanager.feature.create_account.create_account_impl.domain.usecase.CreateAccountUseCase
import soft.divan.financemanager.feature.create_account.create_account_impl.domain.usecase.DeleteAccountUseCase
import soft.divan.financemanager.feature.create_account.create_account_impl.domain.usecase.GetAccountByIdUseCase
import soft.divan.financemanager.feature.create_account.create_account_impl.domain.usecase.UpdateAccountUseCase
import soft.divan.financemanager.feature.create_account.create_account_impl.domain.usecase.impl.CreateAccountUseCaseImpl
import soft.divan.financemanager.feature.create_account.create_account_impl.domain.usecase.impl.DeleteAccountUseCaseImpl
import soft.divan.financemanager.feature.create_account.create_account_impl.domain.usecase.impl.GetAccountByIdUseCaseImpl
import soft.divan.financemanager.feature.create_account.create_account_impl.domain.usecase.impl.UpdateAccountUseCaseImpl
import soft.divan.financemanager.feature.create_account.create_account_impl.navigation.CreateAccountFeatureImpl

@Module
@InstallIn(SingletonComponent::class)
interface CreateAccountBinderModule {

    @Binds
    fun bindTransactionRouter(impl: CreateAccountFeatureImpl): CreateAccountFeatureApi

    @Binds
    fun bindCreateAccountUserCase(impl: CreateAccountUseCaseImpl): CreateAccountUseCase

    @Binds
    fun bindUpdateAccountUserCase(impl: UpdateAccountUseCaseImpl): UpdateAccountUseCase

    @Binds
    fun bindDeleteAccountUserCase(impl: DeleteAccountUseCaseImpl): DeleteAccountUseCase

    @Binds
    fun bindGetAccountByIdUserCase(impl: GetAccountByIdUseCaseImpl): GetAccountByIdUseCase
}