package soft.divan.financemanager.sync.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import soft.divan.financemanager.sync.data.repository.SyncRepositoryImpl
import soft.divan.financemanager.sync.domain.repository.SyncRepository
import soft.divan.financemanager.sync.domain.usecase.ObserveSyncIntervalHoursUseCase
import soft.divan.financemanager.sync.domain.usecase.SetLastSyncTimeUseCase
import soft.divan.financemanager.sync.domain.usecase.impl.ObserveSyncIntervalHoursUseCaseImpl
import soft.divan.financemanager.sync.domain.usecase.impl.SetLastSyncTimeUseCaseImpl
import soft.divan.financemanager.sync.scheduler.SyncScheduler
import soft.divan.financemanager.sync.scheduler.WorkManagerSyncScheduler
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface SyncBinderModule {

    @Singleton
    @Binds
    fun bindAppLocaleManager(impl: WorkManagerSyncScheduler): SyncScheduler

    @Binds
    @Singleton
    fun bindSyncRepository(impl: SyncRepositoryImpl): SyncRepository

    @Binds
    fun bindSetLastSyncTimeUseCaseCase(impl: SetLastSyncTimeUseCaseImpl): SetLastSyncTimeUseCase

    @Binds
    fun bindObserveSyncIntervalHoursUseCase(
        impl: ObserveSyncIntervalHoursUseCaseImpl
    ): ObserveSyncIntervalHoursUseCase
}
// Revue me>>
