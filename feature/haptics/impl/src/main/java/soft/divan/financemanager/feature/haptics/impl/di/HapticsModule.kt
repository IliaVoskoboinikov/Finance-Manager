package soft.divan.financemanager.feature.haptics.impl.di

import android.content.Context
import android.os.Build
import android.os.Vibrator
import android.os.VibratorManager
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import soft.divan.financemanager.feature.haptics.api.domain.HapticsManager
import soft.divan.financemanager.feature.haptics.impl.data.haptics.HapticsManagerImpl
import soft.divan.financemanager.feature.haptics.impl.data.sourсe.HapticsLocalSource
import soft.divan.financemanager.feature.haptics.impl.data.sourсe.impl.HapticsLocalSourceImpl
import soft.divan.financemanager.feature.haptics.impl.domain.usecase.ObserveHapticsEnabledUseCase
import javax.inject.Singleton

val Context.hapticsDataStore: DataStore<Preferences> by preferencesDataStore("haptics_preferences")

@Module
@InstallIn(SingletonComponent::class)
object HapticModule {

    @Provides
    @Singleton
    fun provideVibrator(@ApplicationContext context: Context): Vibrator =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(VibratorManager::class.java)
            manager?.defaultVibrator
        } else {
            context.getSystemService(Vibrator::class.java)
        } ?: error("Vibrator not available")

    @Provides
    @Singleton
    fun provideHapticsManager(
        vibrator: Vibrator,
        observeHapticsEnabled: ObserveHapticsEnabledUseCase,
        @ApplicationScope scope: CoroutineScope
    ): HapticsManager =
        HapticsManagerImpl(vibrator, observeHapticsEnabled, scope)


    @Provides
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Provides
    @Singleton
    @HapticsDataStore
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.hapticsDataStore
    }

    @Provides
    @Singleton
    fun provideHapticsPreferences(@HapticsDataStore dataStore: DataStore<Preferences>): HapticsLocalSource {
        return HapticsLocalSourceImpl(dataStore)
    }
}