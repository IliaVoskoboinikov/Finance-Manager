package soft.divan.financemanager.feature.haptic.haptic_impl.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import soft.divan.financemanager.feature.haptic.haptic_api.HapticFeatureApi
import soft.divan.financemanager.feature.haptic.haptic_impl.data.repository.HapticRepositoryImpl
import soft.divan.financemanager.feature.haptic.haptic_impl.domain.repository.HapticRepository
import soft.divan.financemanager.feature.haptic.haptic_impl.domain.usecase.ObserveHapticEnabledUseCase
import soft.divan.financemanager.feature.haptic.haptic_impl.domain.usecase.SetHapticEnabledUseCase
import soft.divan.financemanager.feature.haptic.haptic_impl.domain.usecase.impl.ObserveHapticEnabledUseCaseImpl
import soft.divan.financemanager.feature.haptic.haptic_impl.domain.usecase.impl.SetHapticEnabledUseCaseImpl
import soft.divan.financemanager.feature.haptic.haptic_impl.navigation.HapticFeatureImpl
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
interface HapticBinderModule {

    @Binds
    fun bindHapticRouter(impl: HapticFeatureImpl): HapticFeatureApi

    @Binds
    @Singleton
    fun bindHapticRepository(impl: HapticRepositoryImpl): HapticRepository

    @Binds
    fun bindObserveHapticEnabledUseCase(impl: ObserveHapticEnabledUseCaseImpl): ObserveHapticEnabledUseCase

    @Binds
    fun bindSetHapticEnabledUseCase(impl: SetHapticEnabledUseCaseImpl): SetHapticEnabledUseCase

}
