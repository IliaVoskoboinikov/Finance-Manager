package soft.divan.financemanager.feature.security.security_impl.di

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import soft.divan.financemanager.feature.security.security_api.SecurityFeatureApi
import soft.divan.financemanager.feature.security.security_impl.data.repository.SecurityRepositoryImpl
import soft.divan.financemanager.feature.security.security_impl.data.sourse.SecurityLocalDataSource
import soft.divan.financemanager.feature.security.security_impl.data.sourse.impl.SecurityLocalDataSourceImpl
import soft.divan.financemanager.feature.security.security_impl.domain.repository.SecurityRepository
import soft.divan.financemanager.feature.security.security_impl.domain.usecase.DeletePinUseCase
import soft.divan.financemanager.feature.security.security_impl.domain.usecase.GetSavedPinUseCase
import soft.divan.financemanager.feature.security.security_impl.domain.usecase.IsPinSetUseCase
import soft.divan.financemanager.feature.security.security_impl.domain.usecase.SavePinUseCase
import soft.divan.financemanager.feature.security.security_impl.domain.usecase.impl.DeletePinUseCaseImpl
import soft.divan.financemanager.feature.security.security_impl.domain.usecase.impl.GetSavedPinUseCaseImpl
import soft.divan.financemanager.feature.security.security_impl.domain.usecase.impl.IsPinSetUseCaseImpl
import soft.divan.financemanager.feature.security.security_impl.domain.usecase.impl.SavePinUseCaseImpl
import soft.divan.financemanager.feature.security.security_impl.navigation.SecurityFeatureImpl
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
interface SecurityModule {

    @Binds
    fun bindSecurityRouter(impl: SecurityFeatureImpl): SecurityFeatureApi

    @Binds
    fun bindSavePinUseCase(impl: SavePinUseCaseImpl): SavePinUseCase

    @Binds
    fun bindGetSavedPinUseCase(impl: GetSavedPinUseCaseImpl): GetSavedPinUseCase

    @Binds
    fun bindIsPinSetUseCase(impl: IsPinSetUseCaseImpl): IsPinSetUseCase

    @Binds
    @Singleton
    fun provideSecurityRepository(impl: SecurityRepositoryImpl): SecurityRepository

    @Binds
    @Singleton
    fun bindSecurityLocalDataSource(impl: SecurityLocalDataSourceImpl): SecurityLocalDataSource

    @Binds
    fun bindDeletePinUseCase(impl: DeletePinUseCaseImpl): DeletePinUseCase

}

@Module
@InstallIn(SingletonComponent::class)
object SecurityProvidesModule {

    @Provides
    @Singleton
    fun provideSecurityLocalDataSourceImpl(
        @ApplicationContext context: Context
    ): SecurityLocalDataSourceImpl = SecurityLocalDataSourceImpl(context)

}