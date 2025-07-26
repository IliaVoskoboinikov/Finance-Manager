package soft.divan.financemanager.feature.settings.settings_impl.domain.repositiry

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.feature.settings.settings_impl.domain.ThemeMode

interface ThemeRepository {
    fun getThemeMode(): Flow<ThemeMode>
    suspend fun setThemeMode(mode: ThemeMode)
}
