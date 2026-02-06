package soft.divan.financemanager.feature.sounds.impl.domain.usecase

import kotlinx.coroutines.flow.Flow

interface ObserveSoundsEnabledUseCase {
    operator fun invoke(): Flow<Boolean>
}
// Revue me>>
