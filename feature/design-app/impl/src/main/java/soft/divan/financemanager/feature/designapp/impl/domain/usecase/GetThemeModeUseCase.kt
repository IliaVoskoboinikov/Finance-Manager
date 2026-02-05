package soft.divan.financemanager.feature.designapp.impl.domain.usecase

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.feature.designapp.impl.domain.model.ThemeMode

interface GetThemeModeUseCase {
    operator fun invoke(): Flow<ThemeMode>
}
