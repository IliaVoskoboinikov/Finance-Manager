package soft.divan.financemanager.feature.design_app.design_app_impl.domain.usecase

import soft.divan.financemanager.feature.design_app.design_app_impl.domain.model.ThemeMode

interface SetThemeModeUseCase {
    suspend operator fun invoke(mode: ThemeMode)
}