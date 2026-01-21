package soft.divan.financemanager.feature.account.impl.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import soft.divan.financemanager.feature.account.api.AccountFeatureApi
import soft.divan.financemanager.feature.account.impl.domain.usecase.CreateAccountUseCase
import soft.divan.financemanager.feature.account.impl.domain.usecase.DeleteAccountUseCase
import soft.divan.financemanager.feature.account.impl.domain.usecase.GetAccountByIdUseCase
import soft.divan.financemanager.feature.account.impl.domain.usecase.UpdateAccountUseCase
import soft.divan.financemanager.feature.account.impl.domain.usecase.impl.CreateAccountUseCaseImpl
import soft.divan.financemanager.feature.account.impl.domain.usecase.impl.DeleteAccountUseCaseImpl
import soft.divan.financemanager.feature.account.impl.domain.usecase.impl.GetAccountByIdUseCaseImpl
import soft.divan.financemanager.feature.account.impl.domain.usecase.impl.UpdateAccountUseCaseImpl
import soft.divan.financemanager.feature.account.impl.navigation.AccountFeatureImpl

@Module
@InstallIn(SingletonComponent::class)
interface AccountBinderModule {

    @Binds
    fun bindAccountRouter(impl: AccountFeatureImpl): AccountFeatureApi

    @Binds
    fun bindCreateAccountUserCase(impl: CreateAccountUseCaseImpl): CreateAccountUseCase

    @Binds
    fun bindUpdateAccountUserCase(impl: UpdateAccountUseCaseImpl): UpdateAccountUseCase

    @Binds
    fun bindDeleteAccountUserCase(impl: DeleteAccountUseCaseImpl): DeleteAccountUseCase

    @Binds
    fun bindGetAccountByIdUserCase(impl: GetAccountByIdUseCaseImpl): GetAccountByIdUseCase
}
