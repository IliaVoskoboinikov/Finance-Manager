package soft.divan.financemanager.feature.settings.settings_impl.data


import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import soft.divan.financemanager.feature.settings.settings_impl.domain.ThemeMode
import soft.divan.financemanager.feature.settings.settings_impl.domain.repositiry.ThemeRepository
import javax.inject.Inject


class ThemeRepositoryImpl @Inject constructor(private val dataStore: DataStore<Preferences>) :

    ThemeRepository {
    private val key = stringPreferencesKey("app_theme_mode")

    override fun getThemeMode(): Flow<ThemeMode> {
        return dataStore.data.map { prefs ->
            when (prefs[key]) {
                "DARK" -> ThemeMode.DARK
                else -> ThemeMode.LIGHT
            }
        }
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { prefs ->
            prefs[key] = mode.name
        }
    }
}
