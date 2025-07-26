package soft.divan.financemanager.feature.settings.settings_impl.domain.usecase

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.feature.settings.settings_impl.domain.ThemeMode

interface GetThemeModeUseCase {
    operator fun invoke(): Flow<ThemeMode>
}