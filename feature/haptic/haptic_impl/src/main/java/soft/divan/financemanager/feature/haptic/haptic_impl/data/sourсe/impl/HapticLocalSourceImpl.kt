package soft.divan.financemanager.feature.haptic.haptic_impl.data.sourсe.impl

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import soft.divan.financemanager.feature.haptic.haptic_impl.data.sourсe.HapticLocalSource
import javax.inject.Inject

private val KEY_HAPTIC_ENABLED = stringPreferencesKey("app_haptic_enabled")

class HapticLocalSourceImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : HapticLocalSource {

    override fun getHapticEnabled(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[KEY_HAPTIC_ENABLED].toBoolean()
        }
    }

    override suspend fun setHapticEnabled(isEnabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_HAPTIC_ENABLED] = isEnabled.toString()
        }
    }
}