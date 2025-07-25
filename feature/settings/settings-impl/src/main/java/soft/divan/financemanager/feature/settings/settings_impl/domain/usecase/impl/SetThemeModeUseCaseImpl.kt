package soft.divan.financemanager.feature.settings.settings_impl.domain.usecase.impl

import soft.divan.financemanager.feature.settings.settings_impl.domain.ThemeMode
import soft.divan.financemanager.feature.settings.settings_impl.domain.repositiry.ThemeRepository
import soft.divan.financemanager.feature.settings.settings_impl.domain.usecase.SetThemeModeUseCase
import javax.inject.Inject

class SetThemeModeUseCaseImpl @Inject constructor(
    private val themeRepository: ThemeRepository
) : SetThemeModeUseCase {
    override suspend operator fun invoke(mode: ThemeMode) {
        themeRepository.setThemeMode(mode)
    }
}