package soft.divan.financemanager.feature.settings.settings_impl.data


import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import soft.divan.financemanager.feature.settings.settings_impl.domain.ThemeMode
import soft.divan.financemanager.feature.settings.settings_impl.domain.repositiry.ThemeRepository
import soft.divan.financemanager.uikit.theme.AccentColor
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

    private val colorKey = stringPreferencesKey("app_accent_color")

    override fun getAccentColor(): Flow<AccentColor> {
        return dataStore.data.map { prefs ->
            when (prefs[colorKey]) {
                "PURPLE" -> AccentColor.PURPLE
                "ORANGE" -> AccentColor.ORANGE
                "BLUE" -> AccentColor.BLUE
                "PINK" -> AccentColor.PINK
                else -> AccentColor.MINT
            }
        }
    }

    override suspend fun setAccentColor(color: AccentColor) {
        dataStore.edit { prefs ->
            prefs[colorKey] = color.name
        }
    }

}
