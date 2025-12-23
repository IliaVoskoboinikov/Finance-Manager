package soft.divan.financemanager.feature.design_app.design_app_impl.domain.usecase

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.Flow

interface GetCustomAccentColorUseCase {
    operator fun invoke(): Flow<Color?>
}