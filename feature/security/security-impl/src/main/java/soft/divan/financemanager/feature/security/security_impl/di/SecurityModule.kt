package soft.divan.financemanager.feature.security.security_impl.di

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import soft.divan.financemanager.feature.security.security_api.SecurityFeatureApi
import soft.divan.financemanager.feature.security.security_impl.data.SecurityRepositoryImpl
import soft.divan.financemanager.feature.security.security_impl.domain.repository.SecurityRepository
import soft.divan.financemanager.feature.security.security_impl.domain.usecase.GetSavedPinUseCase
import soft.divan.financemanager.feature.security.security_impl.domain.usecase.IsPinSetUseCase
import soft.divan.financemanager.feature.security.security_impl.domain.usecase.SavePinUseCase
import soft.divan.financemanager.feature.security.security_impl.domain.usecase.impl.GetSavedPinUseCaseImpl
import soft.divan.financemanager.feature.security.security_impl.domain.usecase.impl.IsPinSetUseCaseImpl
import soft.divan.financemanager.feature.security.security_impl.domain.usecase.impl.SavePinUseCaseImpl
import soft.divan.financemanager.feature.security.security_impl.navigation.SecurityFeatureImpl


@Module
@InstallIn(SingletonComponent::class)
abstract class SecurityModule {

    @Binds
    abstract fun bindSecurityRouter(impl: SecurityFeatureImpl): SecurityFeatureApi

    @Binds
    abstract fun bindSavePinUseCase(impl: SavePinUseCaseImpl): SavePinUseCase

    @Binds
    abstract fun bindGetSavedPinUseCase(impl: GetSavedPinUseCaseImpl): GetSavedPinUseCase

    @Binds
    abstract fun bindIsPinSetUseCase(impl: IsPinSetUseCaseImpl): IsPinSetUseCase

    @Binds
    abstract fun provideSecurityRepository(impl: SecurityRepositoryImpl): SecurityRepository

}

@Module
@InstallIn(SingletonComponent::class)
object SecurityProvidesModule {

    @Provides
    fun provideSecurityRepository(
        @ApplicationContext context: Context
    ): SecurityRepositoryImpl = SecurityRepositoryImpl(context)


}