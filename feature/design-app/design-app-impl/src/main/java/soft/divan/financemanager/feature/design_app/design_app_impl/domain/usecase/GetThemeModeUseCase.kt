package soft.divan.financemanager.feature.design_app.design_app_impl.domain.usecase

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.feature.design_app.design_app_impl.domain.model.ThemeMode

interface GetThemeModeUseCase {
    operator fun invoke(): Flow<ThemeMode>
}