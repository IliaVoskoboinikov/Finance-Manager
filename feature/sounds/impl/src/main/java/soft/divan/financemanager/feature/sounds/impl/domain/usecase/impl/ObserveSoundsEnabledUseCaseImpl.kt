package soft.divan.financemanager.feature.sounds.impl.domain.usecase.impl

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.feature.sounds.impl.domain.repository.SoundsRepository
import soft.divan.financemanager.feature.sounds.impl.domain.usecase.ObserveSoundsEnabledUseCase
import javax.inject.Inject

class ObserveSoundsEnabledUseCaseImpl @Inject constructor(
    private val repository: SoundsRepository
) : ObserveSoundsEnabledUseCase {
    override operator fun invoke(): Flow<Boolean> = repository.observeSoundsEnabled()
}
