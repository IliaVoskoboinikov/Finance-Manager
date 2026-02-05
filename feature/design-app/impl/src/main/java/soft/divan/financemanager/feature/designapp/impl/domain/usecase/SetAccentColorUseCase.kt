package soft.divan.financemanager.feature.designapp.impl.domain.usecase

import soft.divan.financemanager.uikit.theme.AccentColor

interface SetAccentColorUseCase {
    suspend operator fun invoke(color: AccentColor)
}
