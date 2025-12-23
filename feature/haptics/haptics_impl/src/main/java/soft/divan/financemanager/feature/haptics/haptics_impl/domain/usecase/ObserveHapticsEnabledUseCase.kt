package soft.divan.financemanager.feature.haptics.haptics_impl.domain.usecase

import kotlinx.coroutines.flow.Flow

interface ObserveHapticsEnabledUseCase {
    operator fun invoke(): Flow<Boolean>
}