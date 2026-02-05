package soft.divan.financemanager.feature.sounds.impl.domain.usecase.impl

import soft.divan.financemanager.feature.sounds.impl.domain.repository.SoundsRepository
import soft.divan.financemanager.feature.sounds.impl.domain.usecase.SetSoundsEnabledUseCase
import javax.inject.Inject

class SetSoundsEnabledUseCaseImpl @Inject constructor(
    private val repository: SoundsRepository
) : SetSoundsEnabledUseCase {
    override suspend fun invoke(isEnabled: Boolean) {
        repository.setSoundsEnabled(isEnabled)
    }
}
