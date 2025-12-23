package soft.divan.financemanager.feature.haptic.haptic_impl.domain.usecase.impl

import soft.divan.financemanager.feature.haptic.haptic_impl.domain.repository.HapticRepository
import soft.divan.financemanager.feature.haptic.haptic_impl.domain.usecase.SetHapticEnabledUseCase
import javax.inject.Inject

class SetHapticEnabledUseCaseImpl @Inject constructor(
    private val repository: HapticRepository
) : SetHapticEnabledUseCase {
    override suspend fun invoke(isEnabled: Boolean) {
        repository.setHapticEnabled(isEnabled)
    }
}