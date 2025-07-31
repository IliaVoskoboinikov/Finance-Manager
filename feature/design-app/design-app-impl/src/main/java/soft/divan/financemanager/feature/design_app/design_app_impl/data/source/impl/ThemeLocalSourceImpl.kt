package soft.divan.financemanager.feature.design_app.design_app_impl.data.source.impl

import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import soft.divan.financemanager.feature.design_app.design_app_impl.data.source.ThemeLocalSource
import soft.divan.financemanager.feature.design_app.design_app_impl.domain.model.ThemeMode
import soft.divan.financemanager.uikit.theme.AccentColor
import javax.inject.Inject

class ThemeLocalSourceImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : ThemeLocalSource {
    private val key = stringPreferencesKey("app_theme_mode")

    override fun getThemeMode(): Flow<ThemeMode> {
        return dataStore.data.map { prefs ->
            when (prefs[key]) {
                "LIGHT" -> ThemeMode.LIGHT
                "DARK" -> ThemeMode.DARK
                else -> ThemeMode.SYSTEM
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
                "DYNAMIC" -> AccentColor.DYNAMIC
                "CUSTOM" -> AccentColor.CUSTOM
                else -> AccentColor.MINT
            }
        }
    }

    override suspend fun setAccentColor(color: AccentColor) {
        dataStore.edit { prefs ->
            prefs[colorKey] = color.name
        }
    }


    private val customColorKey = stringPreferencesKey("app_custom_accent_hex")

    override fun getCustomAccentColor(): Flow<Color?> {
        return dataStore.data.map { prefs ->
            prefs[customColorKey]?.let { hex ->
                runCatching { Color(hex.toColorInt()) }.getOrNull()
            }
        }
    }

    override suspend fun setCustomAccentColor(color: Color) {
        val hex = color.toHexString()
        dataStore.edit { prefs ->
            prefs[customColorKey] = hex
        }
    }

    //todo utils
    private fun Color.toHexString(): String {
        val r = (red * 255).toInt().coerceIn(0, 255).toString(16).padStart(2, '0')
        val g = (green * 255).toInt().coerceIn(0, 255).toString(16).padStart(2, '0')
        val b = (blue * 255).toInt().coerceIn(0, 255).toString(16).padStart(2, '0')
        return "#${r}${g}${b}".uppercase()
    }
}

