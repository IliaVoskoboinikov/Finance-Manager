package soft.divan.financemanager.feature.designapp.impl.domain.usecase

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.uikit.theme.AccentColor

interface GetAccentColorUseCase {
    operator fun invoke(): Flow<AccentColor>
}
