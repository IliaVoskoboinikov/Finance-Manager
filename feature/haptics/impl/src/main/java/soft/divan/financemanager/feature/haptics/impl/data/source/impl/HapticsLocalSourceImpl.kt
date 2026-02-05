package soft.divan.financemanager.feature.haptics.impl.data.source.impl

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import soft.divan.financemanager.feature.haptics.impl.data.source.HapticsLocalSource
import javax.inject.Inject

private val KEY_HAPTIC_ENABLED = booleanPreferencesKey("app_haptics_enabled")

class HapticsLocalSourceImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : HapticsLocalSource {

    override fun getHapticsEnabled(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[KEY_HAPTIC_ENABLED] ?: true
        }
    }

    override suspend fun setHapticsEnabled(isEnabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_HAPTIC_ENABLED] = isEnabled
        }
    }
}
