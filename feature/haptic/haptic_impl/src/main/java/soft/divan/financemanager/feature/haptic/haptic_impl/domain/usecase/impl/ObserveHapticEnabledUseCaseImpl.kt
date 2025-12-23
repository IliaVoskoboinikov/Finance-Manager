package soft.divan.financemanager.feature.haptic.haptic_impl.domain.usecase.impl

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.feature.haptic.haptic_impl.domain.repository.HapticRepository
import soft.divan.financemanager.feature.haptic.haptic_impl.domain.usecase.ObserveHapticEnabledUseCase
import javax.inject.Inject

class ObserveHapticEnabledUseCaseImpl @Inject constructor(
    private val repository: HapticRepository
) : ObserveHapticEnabledUseCase {
    override operator fun invoke(): Flow<Boolean> = repository.observeHapticEnabled()
}
