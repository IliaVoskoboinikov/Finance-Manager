package soft.divan.financemanager.feature.design_app.design_app_impl.data


import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.feature.design_app.design_app_impl.data.source.ThemeLocalSource
import soft.divan.financemanager.feature.design_app.design_app_impl.domain.model.ThemeMode
import soft.divan.financemanager.feature.design_app.design_app_impl.domain.repositiry.ThemeRepository
import soft.divan.financemanager.uikit.theme.AccentColor
import javax.inject.Inject


class ThemeRepositoryImpl @Inject constructor(
    private val themeLocalSource: ThemeLocalSource
) : ThemeRepository {

    override fun getThemeMode(): Flow<ThemeMode> {
        return themeLocalSource.getThemeMode()
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        themeLocalSource.setThemeMode(mode)
    }

    override fun getAccentColor(): Flow<AccentColor> {
        return themeLocalSource.getAccentColor()
    }

    override suspend fun setAccentColor(color: AccentColor) {
        themeLocalSource.setAccentColor(color)
    }

    override fun getCustomAccentColor(): Flow<Color?> {
        return themeLocalSource.getCustomAccentColor()
    }

    override suspend fun setCustomAccentColor(color: Color) {
        themeLocalSource.setCustomAccentColor(color)
    }

}
