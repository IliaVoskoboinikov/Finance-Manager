package soft.divan.financemanager.feature.design_app.design_app_impl.domain.usecase.impl

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.feature.design_app.design_app_impl.domain.model.ThemeMode
import soft.divan.financemanager.feature.design_app.design_app_impl.domain.repositiry.ThemeRepository
import soft.divan.financemanager.feature.design_app.design_app_impl.domain.usecase.GetThemeModeUseCase

import javax.inject.Inject

class GetThemeModeUseCaseImpl @Inject constructor(
    private val themeRepository: ThemeRepository
) : GetThemeModeUseCase {
    override operator fun invoke(): Flow<ThemeMode> {
        return themeRepository.getThemeMode()
    }
}