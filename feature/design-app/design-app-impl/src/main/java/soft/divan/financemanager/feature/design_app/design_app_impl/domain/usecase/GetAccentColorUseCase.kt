package soft.divan.financemanager.feature.design_app.design_app_impl.domain.usecase

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.uikit.theme.AccentColor

interface GetAccentColorUseCase {
    operator fun invoke(): Flow<AccentColor>
}