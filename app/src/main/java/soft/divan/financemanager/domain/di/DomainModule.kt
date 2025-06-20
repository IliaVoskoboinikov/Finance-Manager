package soft.divan.financemanager.domain.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import soft.divan.financemanager.domain.usecase.account.GetAccountsUseCase
import soft.divan.financemanager.domain.usecase.account.UpdateAccountUseCase
import soft.divan.financemanager.domain.usecase.account.impl.GetAccountsUseCaseImpl
import soft.divan.financemanager.domain.usecase.account.impl.UpdateAccountUseCaseImpl

@Module
@InstallIn(SingletonComponent::class)
object DomainModule {


    @Provides
    fun provideGetAccountsUseCaseImpl(
        impl: GetAccountsUseCaseImpl
    ): GetAccountsUseCase {
        return impl
    }

    @Provides
    fun provideUpdateAccountsUseCaseImpl(
        impl: UpdateAccountUseCaseImpl
    ): UpdateAccountUseCase {
        return impl
    }

}