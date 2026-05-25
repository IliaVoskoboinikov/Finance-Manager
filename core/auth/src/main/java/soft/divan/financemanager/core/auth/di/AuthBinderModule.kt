package soft.divan.financemanager.core.auth.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import soft.divan.financemanager.core.auth.domain.usecase.GetAuthStatusUseCase
import soft.divan.financemanager.core.auth.domain.usecase.SetAuthStatusUseCase
import soft.divan.financemanager.core.auth.domain.usecase.impl.GetAuthStatusUseCaseImpl
import soft.divan.financemanager.core.auth.domain.usecase.impl.SetAuthStatusUseCaseImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface AuthBinderModule {

    @Binds
    @Singleton
    fun bindGetAuthStatusUseCase(impl: GetAuthStatusUseCaseImpl): GetAuthStatusUseCase

    @Binds
    @Singleton
    fun bindSetAuthStatusUseCase(impl: SetAuthStatusUseCaseImpl): SetAuthStatusUseCase
}
