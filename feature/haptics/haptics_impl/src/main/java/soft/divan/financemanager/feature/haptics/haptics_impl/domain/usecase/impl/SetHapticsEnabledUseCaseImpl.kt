package soft.divan.financemanager.feature.haptics.haptics_impl.domain.usecase.impl

import soft.divan.financemanager.feature.haptics.haptics_impl.domain.repository.HapticsRepository
import soft.divan.financemanager.feature.haptics.haptics_impl.domain.usecase.SetHapticsEnabledUseCase
import javax.inject.Inject

class SetHapticsEnabledUseCaseImpl @Inject constructor(
    private val repository: HapticsRepository
) : SetHapticsEnabledUseCase {
    override suspend fun invoke(isEnabled: Boolean) {
        repository.setHapticsEnabled(isEnabled)
    }
}