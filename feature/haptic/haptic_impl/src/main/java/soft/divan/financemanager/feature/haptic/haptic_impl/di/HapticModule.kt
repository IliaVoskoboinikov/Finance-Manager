package soft.divan.financemanager.feature.haptic.haptic_impl.di

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
import soft.divan.financemanager.feature.haptic.haptic_api.domain.HapticManager
import soft.divan.financemanager.feature.haptic.haptic_impl.data.haptic.HapticManagerImpl
import soft.divan.financemanager.feature.haptic.haptic_impl.data.sourсe.HapticLocalSource
import soft.divan.financemanager.feature.haptic.haptic_impl.data.sourсe.impl.HapticLocalSourceImpl
import soft.divan.financemanager.feature.haptic.haptic_impl.domain.usecase.ObserveHapticEnabledUseCase
import javax.inject.Singleton

val Context.hapticDataStore: DataStore<Preferences> by preferencesDataStore("haptic_preferences")

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
    fun provideHapticManager(
        vibrator: Vibrator,
        observeHapticEnabled: ObserveHapticEnabledUseCase,
        @ApplicationScope scope: CoroutineScope
    ): HapticManager =
        HapticManagerImpl(vibrator, observeHapticEnabled, scope)


    @Provides
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Provides
    @Singleton
    @HapticDataStore
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.hapticDataStore
    }

    @Provides
    @Singleton
    fun provideThemePreferences(@HapticDataStore dataStore: DataStore<Preferences>): HapticLocalSource {
        return HapticLocalSourceImpl(dataStore)
    }
}