package soft.divan.financemanager.feature.design_app.design_app_impl.domain.usecase.impl

import soft.divan.financemanager.feature.design_app.design_app_impl.domain.repositiry.ThemeRepository
import soft.divan.financemanager.feature.design_app.design_app_impl.domain.usecase.SetAccentColorUseCase
import soft.divan.financemanager.uikit.theme.AccentColor
import javax.inject.Inject

class SetAccentColorUseCaseImpl @Inject constructor(
    private val repository: ThemeRepository
) : SetAccentColorUseCase {
    override suspend fun invoke(color: AccentColor) = repository.setAccentColor(color)
}