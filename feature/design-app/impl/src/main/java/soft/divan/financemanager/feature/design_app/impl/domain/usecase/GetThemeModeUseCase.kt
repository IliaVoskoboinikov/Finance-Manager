package soft.divan.financemanager.feature.design_app.impl.domain.usecase

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.feature.design_app.impl.domain.model.ThemeMode

interface GetThemeModeUseCase {
    operator fun invoke(): Flow<ThemeMode>
}