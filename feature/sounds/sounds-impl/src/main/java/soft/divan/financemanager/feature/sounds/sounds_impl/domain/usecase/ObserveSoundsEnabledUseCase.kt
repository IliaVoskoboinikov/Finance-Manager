package soft.divan.financemanager.feature.sounds.sounds_impl.domain.usecase

import kotlinx.coroutines.flow.Flow

interface ObserveSoundsEnabledUseCase {
    operator fun invoke(): Flow<Boolean>
}