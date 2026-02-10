package soft.divan.financemanager.feature.sounds.impl.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import soft.divan.financemanager.feature.sounds.api.SoundsFeatureApi
import soft.divan.financemanager.feature.sounds.impl.data.repository.SoundsRepositoryImpl
import soft.divan.financemanager.feature.sounds.impl.domain.repository.SoundsRepository
import soft.divan.financemanager.feature.sounds.impl.domain.usecase.ObserveSoundsEnabledUseCase
import soft.divan.financemanager.feature.sounds.impl.domain.usecase.SetSoundsEnabledUseCase
import soft.divan.financemanager.feature.sounds.impl.domain.usecase.impl.ObserveSoundsEnabledUseCaseImpl
import soft.divan.financemanager.feature.sounds.impl.domain.usecase.impl.SetSoundsEnabledUseCaseImpl
import soft.divan.financemanager.feature.sounds.impl.navigation.SoundsFeatureImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface SoundsBinderModule {

    @Binds
    fun bindSoundsRouter(impl: SoundsFeatureImpl): SoundsFeatureApi

    @Binds
    @Singleton
    fun bindSoundsRepository(impl: SoundsRepositoryImpl): SoundsRepository

    @Binds
    fun bindObserveSoundsEnabledUseCase(
        impl: ObserveSoundsEnabledUseCaseImpl
    ): ObserveSoundsEnabledUseCase

    @Binds
    fun bindSetSoundsEnabledUseCase(impl: SetSoundsEnabledUseCaseImpl): SetSoundsEnabledUseCase
}
// Revue me>>
