package soft.divan.financemanager.core.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import javax.inject.Singleton

/** Часы приложения — одна привязка [Clock] на весь граф. */
@Module
@InstallIn(SingletonComponent::class)
object ClockModule {

    /**
     * Стенные часы для всех, кому нужно текущее время: очереди, политики попыток PIN-кода,
     * форм операции и счёта и других.
     *
     * Время не берётся на месте через `System.currentTimeMillis()` / `now()`, а приходит
     * зависимостью, чтобы тесты могли работать на фиксированном времени. Привязка вынесена из
     * модуля очереди, потому что от неё зависят и `core:security`, и фичи.
     *
     * Зона часов — UTC. Из них берётся только `instant()`. Дату и время суток получают через
     * `UiDateFormatter` в зоне устройства: `LocalDate.now(clock)` вернул бы дату по UTC.
     */
    @Provides
    @Singleton
    fun provideClock(): Clock = Clock.systemUTC()
}
