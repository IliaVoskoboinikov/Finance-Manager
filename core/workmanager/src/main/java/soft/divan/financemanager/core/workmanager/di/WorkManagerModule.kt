package soft.divan.financemanager.core.workmanager.di

import android.content.Context
import androidx.work.WorkManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Отдаёт синглтон [WorkManager] в граф.
 *
 * `@Provides`, а не `@Binds`: [WorkManager] — внешний класс со статической фабрикой.
 * Инъекция вместо `WorkManager.getInstance(context)` по месту нужна для тестируемости —
 * планировщики принимают его конструктором и в тестах подменяются моком.
 */
@Module
@InstallIn(SingletonComponent::class)
object WorkManagerModule {

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager =
        WorkManager.getInstance(context)
}
