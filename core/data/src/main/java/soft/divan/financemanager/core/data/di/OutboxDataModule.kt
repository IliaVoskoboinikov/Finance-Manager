package soft.divan.financemanager.core.data.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import soft.divan.financemanager.core.data.outbox.OutboxSender
import soft.divan.financemanager.core.data.outbox.RoutingOutboxSender
import soft.divan.financemanager.core.data.source.OutboxLocalDataSource
import soft.divan.financemanager.core.data.source.impl.OutboxLocalDataSourceImpl
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
