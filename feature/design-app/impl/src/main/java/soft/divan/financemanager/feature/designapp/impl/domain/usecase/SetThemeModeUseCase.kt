package soft.divan.financemanager.feature.designapp.impl.domain.usecase

import soft.divan.financemanager.feature.designapp.impl.domain.model.ThemeMode

interface SetThemeModeUseCase {
    suspend operator fun invoke(mode: ThemeMode)
}
// Revue me>>
