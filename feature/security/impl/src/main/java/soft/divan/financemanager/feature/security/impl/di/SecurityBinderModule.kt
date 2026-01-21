package soft.divan.financemanager.feature.security.impl.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import soft.divan.financemanager.feature.security.api.SecurityFeatureApi
import soft.divan.financemanager.feature.security.impl.data.repository.SecurityRepositoryImpl
import soft.divan.financemanager.feature.security.impl.data.sourse.SecurityLocalDataSource
import soft.divan.financemanager.feature.security.impl.data.sourse.impl.SecurityLocalDataSourceImpl
import soft.divan.financemanager.feature.security.impl.domain.repository.SecurityRepository
import soft.divan.financemanager.feature.security.impl.domain.usecase.DeletePinUseCase
import soft.divan.financemanager.feature.security.impl.domain.usecase.GetSavedPinUseCase
import soft.divan.financemanager.feature.security.impl.domain.usecase.IsPinSetUseCase
import soft.divan.financemanager.feature.security.impl.domain.usecase.SavePinUseCase
import soft.divan.financemanager.feature.security.impl.domain.usecase.impl.DeletePinUseCaseImpl
import soft.divan.financemanager.feature.security.impl.domain.usecase.impl.GetSavedPinUseCaseImpl
import soft.divan.financemanager.feature.security.impl.domain.usecase.impl.IsPinSetUseCaseImpl
import soft.divan.financemanager.feature.security.impl.domain.usecase.impl.SavePinUseCaseImpl
import soft.divan.financemanager.feature.security.impl.navigation.SecurityFeatureImpl
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
interface SecurityBinderModule {

    @Binds
    fun bindSecurityRouter(impl: SecurityFeatureImpl): SecurityFeatureApi

    @Binds
    @Singleton
    fun provideSecurityRepository(impl: SecurityRepositoryImpl): SecurityRepository

    @Binds
    @Singleton
    fun bindSecurityLocalDataSource(impl: SecurityLocalDataSourceImpl): SecurityLocalDataSource

    @Binds
    fun bindSavePinUseCase(impl: SavePinUseCaseImpl): SavePinUseCase

    @Binds
    fun bindGetSavedPinUseCase(impl: GetSavedPinUseCaseImpl): GetSavedPinUseCase

    @Binds
    fun bindIsPinSetUseCase(impl: IsPinSetUseCaseImpl): IsPinSetUseCase

    @Binds
    fun bindDeletePinUseCase(impl: DeletePinUseCaseImpl): DeletePinUseCase

}