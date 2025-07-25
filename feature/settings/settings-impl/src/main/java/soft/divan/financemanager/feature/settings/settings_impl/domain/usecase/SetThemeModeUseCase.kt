package soft.divan.financemanager.feature.settings.settings_impl.domain.usecase

import soft.divan.financemanager.feature.settings.settings_impl.domain.ThemeMode

interface SetThemeModeUseCase {
    suspend operator fun invoke(mode: ThemeMode)
}