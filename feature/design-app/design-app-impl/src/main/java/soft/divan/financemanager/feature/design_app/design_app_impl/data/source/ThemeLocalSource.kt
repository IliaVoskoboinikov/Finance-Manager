package soft.divan.financemanager.feature.design_app.design_app_impl.data.source

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.feature.design_app.design_app_impl.domain.model.ThemeMode
import soft.divan.financemanager.uikit.theme.AccentColor

interface ThemeLocalSource {
    fun getThemeMode(): Flow<ThemeMode>
    suspend fun setThemeMode(mode: ThemeMode)
    fun getAccentColor(): Flow<AccentColor>
    suspend fun setAccentColor(color: AccentColor)
    fun getCustomAccentColor(): Flow<Color?>
    suspend fun setCustomAccentColor(color: Color)
}