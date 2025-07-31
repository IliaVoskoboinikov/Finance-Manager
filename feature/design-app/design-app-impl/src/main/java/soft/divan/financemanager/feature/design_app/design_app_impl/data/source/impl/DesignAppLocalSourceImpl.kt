package soft.divan.financemanager.feature.design_app.design_app_impl.data.source.impl

import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import soft.divan.financemanager.feature.design_app.design_app_impl.data.source.DesignAppLocalSource
import soft.divan.financemanager.feature.design_app.design_app_impl.domain.model.ThemeMode
import soft.divan.financemanager.uikit.theme.AccentColor
import javax.inject.Inject


private val KEY_THEME_MODE = stringPreferencesKey("app_theme_mode")
private val KEY_ACCENT_COLOR = stringPreferencesKey("app_accent_color")
private val KEY_CUSTOM_COLOR = stringPreferencesKey("app_custom_accent_hex")

class DesignAppLocalSourceImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : DesignAppLocalSource {
    override fun getThemeMode(): Flow<ThemeMode> {
        return dataStore.data.map { prefs ->
            prefs[KEY_THEME_MODE]?.toEnumOrNull<ThemeMode>() ?: ThemeMode.SYSTEM
        }
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { prefs ->
            prefs[KEY_THEME_MODE] = mode.name
        }
    }

    override fun getAccentColor(): Flow<AccentColor> {
        return dataStore.data.map { prefs ->
            prefs[KEY_ACCENT_COLOR]?.toEnumOrNull<AccentColor>() ?: AccentColor.MINT
        }
    }

    override suspend fun setAccentColor(color: AccentColor) {
        dataStore.edit { prefs ->
            prefs[KEY_ACCENT_COLOR] = color.name
        }
    }

    override fun getCustomAccentColor(): Flow<Color?> {
        return dataStore.data.map { prefs ->
            prefs[KEY_CUSTOM_COLOR]?.let { hex ->
                runCatching { Color(hex.toColorInt()) }.getOrNull()
            }
        }
    }

    override suspend fun setCustomAccentColor(color: Color) {
        val hex = color.toHexString()
        dataStore.edit { prefs ->
            prefs[KEY_CUSTOM_COLOR] = hex
        }
    }

    //todo utils // val color = "mint".toEnumOrNull<AccentColor>() ->  AccentColor.Mint
    inline fun <reified T : Enum<T>> String.toEnumOrNull(): T? {
        return enumValues<T>().firstOrNull { it.name.equals(this, ignoreCase = true) }
    }

    //todo utils
    fun Color.toHexString(): String {
        val a = (alpha * 255).toInt().coerceIn(0, 255).toString(16).padStart(2, '0')
        val r = (red * 255).toInt().coerceIn(0, 255).toString(16).padStart(2, '0')
        val g = (green * 255).toInt().coerceIn(0, 255).toString(16).padStart(2, '0')
        val b = (blue * 255).toInt().coerceIn(0, 255).toString(16).padStart(2, '0')
        return "#$a$r$g$b".uppercase()
    }

}

