package soft.divan.financemanager.feature.synchronization.impl.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import soft.divan.financemanager.feature.synchronization.api.SynchronizationFeatureApi
import soft.divan.financemanager.feature.synchronization.impl.domain.usecase.ObserveLastSyncTimeUseCase
import soft.divan.financemanager.feature.synchronization.impl.domain.usecase.SetSyncIntervalHoursUseCase
import soft.divan.financemanager.feature.synchronization.impl.domain.usecase.impl.ObserveLastSyncTimeUseCaseImpl
import soft.divan.financemanager.feature.synchronization.impl.domain.usecase.impl.SetSyncIntervalHoursUseCaseImpl
import soft.divan.financemanager.feature.synchronization.impl.navigation.SynchronizationFeatureImpl


@Module
@InstallIn(SingletonComponent::class)
interface SynchronizationBinderModule {

    @Binds
    fun bindSynchronizationRouter(impl: SynchronizationFeatureImpl): SynchronizationFeatureApi

    @Binds
    fun bindSetSyncIntervalHoursUseCase(impl: SetSyncIntervalHoursUseCaseImpl): SetSyncIntervalHoursUseCase

    @Binds
    fun bindObserveLastSyncTimeUseCase(impl: ObserveLastSyncTimeUseCaseImpl): ObserveLastSyncTimeUseCase
}
