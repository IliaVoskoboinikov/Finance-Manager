package soft.divan.financemanager.core.data.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import soft.divan.financemanager.core.data.outbox.OutboxSender
import soft.divan.financemanager.core.data.outbox.impl.RoutingOutboxSender
import soft.divan.financemanager.core.data.repository.OutboxRepositoryImpl
import soft.divan.financemanager.core.data.source.OutboxLocalDataSource
import soft.divan.financemanager.core.data.source.impl.OutboxLocalDataSourceImpl
import soft.divan.financemanager.core.domain.repository.OutboxRepository
import soft.divan.financemanager.core.domain.usecase.ObserveUnsentOperationsUseCase
import soft.divan.financemanager.core.domain.usecase.RetryUnsentOperationsUseCase
import soft.divan.financemanager.core.domain.usecase.impl.ObserveUnsentOperationsUseCaseImpl
import soft.divan.financemanager.core.domain.usecase.impl.RetryUnsentOperationsUseCaseImpl
import java.time.Clock
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface OutboxDataModule {

    @Binds
    @Singleton
    fun bindOutboxLocalDataSource(impl: OutboxLocalDataSourceImpl): OutboxLocalDataSource

    /** Процессор получает один отправитель — роутер сам выбирает нужный по типу сущности. */
    @Binds
    @Singleton
    fun bindOutboxSender(impl: RoutingOutboxSender): OutboxSender

    @Binds
    @Singleton
    fun bindOutboxRepository(impl: OutboxRepositoryImpl): OutboxRepository

    @Binds
    fun bindObserveUnsentOperationsUseCase(
        impl: ObserveUnsentOperationsUseCaseImpl
    ): ObserveUnsentOperationsUseCase

    @Binds
    fun bindRetryUnsentOperationsUseCase(
        impl: RetryUnsentOperationsUseCaseImpl
    ): RetryUnsentOperationsUseCase

    companion object {

        /**
         * Часы для отметок времени в очереди (постановка, backoff, момент последней попытки).
         *
         * Вынесены в зависимость, а не берутся из `System.currentTimeMillis()` на месте, чтобы
         * планирование повторов можно было проверять тестами с фиксированным временем.
         */
        @Provides
        @Singleton
        fun provideClock(): Clock = Clock.systemUTC()
    }
}
