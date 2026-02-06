package soft.divan.financemanager.feature.sounds.impl.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import soft.divan.common.di.ApplicationScope
import soft.divan.financemanager.feature.sounds.api.domain.SoundPlayer
import soft.divan.financemanager.feature.sounds.impl.data.sounds.SoundPlayerImpl
import soft.divan.financemanager.feature.sounds.impl.data.sounds.SoundsPoolHolder
import soft.divan.financemanager.feature.sounds.impl.data.source.SoundsLocalSource
import soft.divan.financemanager.feature.sounds.impl.data.source.impl.SoundsLocalSourceImpl
import soft.divan.financemanager.feature.sounds.impl.domain.usecase.ObserveSoundsEnabledUseCase
import javax.inject.Singleton

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
    ): SoundPlayer = SoundPlayerImpl(soundsPoolHolder, observeSoundEnabled, scope)

    @Provides
    @Singleton
    @SoundsDataStore
    fun provideSoundsDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.soundDataStore

    @Provides
    @Singleton
    fun provideSoundsPreferences(
        @SoundsDataStore dataStore: DataStore<Preferences>
    ): SoundsLocalSource =
        SoundsLocalSourceImpl(dataStore)
}
// Revue me>>
