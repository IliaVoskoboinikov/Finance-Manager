package soft.divan.financemanager.feature.settings.settings_impl.domain.repositiry

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.feature.settings.settings_impl.domain.ThemeMode
import soft.divan.financemanager.uikit.theme.AccentColor

interface ThemeRepository {
    fun getThemeMode(): Flow<ThemeMode>
    suspend fun setThemeMode(mode: ThemeMode)
    fun getAccentColor(): Flow<AccentColor>
    suspend fun setAccentColor(color: AccentColor)
}
