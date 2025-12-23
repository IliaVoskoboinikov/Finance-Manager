package soft.divan.financemanager.feature.haptics.haptics_impl.domain.usecase.impl

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.feature.haptics.haptics_impl.domain.repository.HapticsRepository
import soft.divan.financemanager.feature.haptics.haptics_impl.domain.usecase.ObserveHapticsEnabledUseCase
import javax.inject.Inject

class ObserveHapticsEnabledUseCaseImpl @Inject constructor(
    private val repository: HapticsRepository
) : ObserveHapticsEnabledUseCase {
    override operator fun invoke(): Flow<Boolean> = repository.observeHapticsEnabled()
}
