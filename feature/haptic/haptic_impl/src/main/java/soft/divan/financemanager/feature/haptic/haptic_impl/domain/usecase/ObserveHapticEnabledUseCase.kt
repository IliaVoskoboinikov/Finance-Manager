package soft.divan.financemanager.feature.haptic.haptic_impl.domain.usecase

import kotlinx.coroutines.flow.Flow

interface ObserveHapticEnabledUseCase {
    operator fun invoke(): Flow<Boolean>
}