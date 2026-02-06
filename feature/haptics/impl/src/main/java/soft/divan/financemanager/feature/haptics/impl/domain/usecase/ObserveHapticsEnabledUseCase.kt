package soft.divan.financemanager.feature.haptics.impl.domain.usecase

import kotlinx.coroutines.flow.Flow

interface ObserveHapticsEnabledUseCase {
    operator fun invoke(): Flow<Boolean>
}
// Revue me>>
