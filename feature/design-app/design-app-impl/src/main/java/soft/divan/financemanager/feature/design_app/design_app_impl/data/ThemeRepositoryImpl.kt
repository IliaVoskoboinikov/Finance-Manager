package soft.divan.financemanager.feature.design_app.design_app_impl.data


import androidx.datastore.preferences.core.stringPreferencesKey
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

    private val colorKey = stringPreferencesKey("app_accent_color")

    override fun getAccentColor(): Flow<AccentColor> {
        return themeLocalSource.getAccentColor()
    }

    override suspend fun setAccentColor(color: AccentColor) {
        themeLocalSource.setAccentColor(color)
    }

}
