package soft.divan.financemanager.feature.sounds.impl.data.source.impl

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import soft.divan.financemanager.feature.sounds.impl.data.source.SoundsLocalSource
import javax.inject.Inject

private val KEY_SOUND_ENABLED = booleanPreferencesKey("app_sound_enabled")
private const val DEFAULT_SOUND_ENABLED = true

class SoundsLocalSourceImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SoundsLocalSource {

    override fun getSoundEnabled(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[KEY_SOUND_ENABLED] ?: DEFAULT_SOUND_ENABLED
        }
    }

    override suspend fun setSoundEnabled(isEnabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_SOUND_ENABLED] = isEnabled
        }
    }
}