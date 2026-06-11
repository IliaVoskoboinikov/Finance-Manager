package soft.divan.financemanager.core.auth.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import soft.divan.financemanager.core.auth.data.provider.AuthStateHolder
import soft.divan.financemanager.core.auth.domain.provider.AuthStateProvider
import soft.divan.financemanager.core.auth.domain.usecase.GetAuthStatusUseCase
import soft.divan.financemanager.core.auth.domain.usecase.impl.GetAuthStatusUseCaseImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface AuthBinderModule {

    @Binds
    @Singleton
    fun bindAuthStateProvider(impl: AuthStateHolder): AuthStateProvider

    @Binds
    @Singleton
    fun bindGetAuthStatusUseCase(impl: GetAuthStatusUseCaseImpl): GetAuthStatusUseCase
}
