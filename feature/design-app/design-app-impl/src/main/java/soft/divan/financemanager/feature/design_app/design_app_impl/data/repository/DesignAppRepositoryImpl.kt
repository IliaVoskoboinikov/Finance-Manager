package soft.divan.financemanager.feature.design_app.design_app_impl.data.repository

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.feature.design_app.design_app_impl.data.source.DesignAppLocalSource
import soft.divan.financemanager.feature.design_app.design_app_impl.domain.model.ThemeMode
import soft.divan.financemanager.feature.design_app.design_app_impl.domain.repositiry.DesignAppRepository
import soft.divan.financemanager.uikit.theme.AccentColor
import javax.inject.Inject

class DesignAppRepositoryImpl @Inject constructor(
    private val designAppLocalSource: DesignAppLocalSource
) : DesignAppRepository {

    override fun getThemeMode(): Flow<ThemeMode> {
        return designAppLocalSource.getThemeMode()
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        designAppLocalSource.setThemeMode(mode)
    }

    override fun getAccentColor(): Flow<AccentColor> {
        return designAppLocalSource.getAccentColor()
    }

    override suspend fun setAccentColor(color: AccentColor) {
        designAppLocalSource.setAccentColor(color)
    }

    override fun getCustomAccentColor(): Flow<Color?> {
        return designAppLocalSource.getCustomAccentColor()
    }

    override suspend fun setCustomAccentColor(color: Color) {
        designAppLocalSource.setCustomAccentColor(color)
    }

}