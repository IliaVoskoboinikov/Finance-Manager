package soft.divan.financemanager.feature.designapp.impl.domain.usecase

import androidx.compose.ui.graphics.Color

interface SetCustomAccentColorUseCase {
    suspend operator fun invoke(color: Color)
}