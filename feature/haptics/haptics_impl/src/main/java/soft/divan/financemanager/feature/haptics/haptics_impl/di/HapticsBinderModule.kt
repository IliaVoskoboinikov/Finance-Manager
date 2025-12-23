package soft.divan.financemanager.feature.haptics.haptics_impl.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import soft.divan.financemanager.feature.haptics.haptics_api.HapticsFeatureApi
import soft.divan.financemanager.feature.haptics.haptics_impl.data.repository.HapticsRepositoryImpl
import soft.divan.financemanager.feature.haptics.haptics_impl.domain.repository.HapticsRepository
import soft.divan.financemanager.feature.haptics.haptics_impl.domain.usecase.ObserveHapticsEnabledUseCase
import soft.divan.financemanager.feature.haptics.haptics_impl.domain.usecase.SetHapticsEnabledUseCase
import soft.divan.financemanager.feature.haptics.haptics_impl.domain.usecase.impl.ObserveHapticsEnabledUseCaseImpl
import soft.divan.financemanager.feature.haptics.haptics_impl.domain.usecase.impl.SetHapticsEnabledUseCaseImpl
import soft.divan.financemanager.feature.haptics.haptics_impl.navigation.HapticFeatureImpl
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
interface HapticsBinderModule {

    @Binds
    fun bindHapticsRouter(impl: HapticFeatureImpl): HapticsFeatureApi

    @Binds
    @Singleton
    fun bindHapticsRepository(impl: HapticsRepositoryImpl): HapticsRepository

    @Binds
    fun bindObserveHapticsEnabledUseCase(impl: ObserveHapticsEnabledUseCaseImpl): ObserveHapticsEnabledUseCase

    @Binds
    fun bindSetHapticsEnabledUseCase(impl: SetHapticsEnabledUseCaseImpl): SetHapticsEnabledUseCase

}
