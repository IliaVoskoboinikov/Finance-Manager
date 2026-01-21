package soft.divan.financemanager.feature.design_app.impl.domain.usecase

import soft.divan.financemanager.feature.design_app.impl.domain.model.ThemeMode

interface SetThemeModeUseCase {
    suspend operator fun invoke(mode: ThemeMode)
}