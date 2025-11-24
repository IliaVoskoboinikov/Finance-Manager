package soft.divan.financemanager.feature.create_account.create_account_impl.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import soft.divan.financemanager.feature.create_account.create_account_api.CreateAccountFeatureApi
import soft.divan.financemanager.feature.create_account.create_account_impl.domain.usecase.CreateAccountUseCase
import soft.divan.financemanager.feature.create_account.create_account_impl.domain.usecase.impl.CreateAccountUseCaseImpl
import soft.divan.financemanager.feature.create_account.create_account_impl.navigation.CreateAccountFeatureImpl

@Module
@InstallIn(SingletonComponent::class)
interface CreateAccountBinderModule {

    @Binds
    fun bindTransactionRouter(impl: CreateAccountFeatureImpl): CreateAccountFeatureApi

    @Binds
    fun bindCreateAccountUserCase(impl: CreateAccountUseCaseImpl): CreateAccountUseCase
}