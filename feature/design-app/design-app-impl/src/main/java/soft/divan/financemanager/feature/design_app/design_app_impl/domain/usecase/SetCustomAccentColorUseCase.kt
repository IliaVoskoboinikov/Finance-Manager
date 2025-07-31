package soft.divan.financemanager.feature.design_app.design_app_impl.domain.usecase

import androidx.compose.ui.graphics.Color

interface SetCustomAccentColorUseCase {
    suspend operator fun invoke(color: Color)
}