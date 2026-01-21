package soft.divan.financemanager.feature.sounds.impl.di

import android.content.Context
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
import soft.divan.financemanager.feature.sounds.api.domain.SoundPlayer
import soft.divan.financemanager.feature.sounds.impl.data.sounds.SoundPlayerImpl
import soft.divan.financemanager.feature.sounds.impl.data.sounds.SoundsPoolHolder
import soft.divan.financemanager.feature.sounds.impl.data.source.SoundsLocalSource
import soft.divan.financemanager.feature.sounds.impl.data.source.impl.SoundsLocalSourceImpl
import soft.divan.financemanager.feature.sounds.impl.domain.usecase.ObserveSoundsEnabledUseCase
import javax.inject.Singleton

val Context.soundDataStore: DataStore<Preferences> by preferencesDataStore("sound_preferences")

@Module
@InstallIn(SingletonComponent::class)
object SoundsModule {

    @Provides
    @Singleton
    fun provideSoundsPoolHolder(
        @ApplicationContext context: Context
    ): SoundsPoolHolder = SoundsPoolHolder(context)

    @Provides
    @Singleton
    fun provideSoundsPlayer(
        soundsPoolHolder: SoundsPoolHolder,
        observeSoundEnabled: ObserveSoundsEnabledUseCase,
        @ApplicationScope scope: CoroutineScope
    ): SoundPlayer =
        SoundPlayerImpl(soundsPoolHolder, observeSoundEnabled, scope)


    @Provides
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Provides
    @Singleton
    @SoundsDataStore
    fun provideSoundsDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.soundDataStore
    }

    @Provides
    @Singleton
    fun provideSoundsPreferences(@SoundsDataStore dataStore: DataStore<Preferences>): SoundsLocalSource {
        return SoundsLocalSourceImpl(dataStore)
    }
}