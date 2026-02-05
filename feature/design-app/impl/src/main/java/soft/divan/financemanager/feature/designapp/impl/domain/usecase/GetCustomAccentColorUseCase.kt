package soft.divan.financemanager.feature.designapp.impl.domain.usecase

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.Flow

interface GetCustomAccentColorUseCase {
    operator fun invoke(): Flow<Color?>
}
