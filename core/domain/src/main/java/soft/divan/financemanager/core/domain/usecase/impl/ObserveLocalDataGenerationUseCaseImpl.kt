package soft.divan.financemanager.core.domain.usecase.impl

import kotlinx.coroutines.flow.StateFlow
import soft.divan.financemanager.core.domain.repository.LocalDataAccessRepository
import soft.divan.financemanager.core.domain.usecase.ObserveLocalDataGenerationUseCase
import javax.inject.Inject

class ObserveLocalDataGenerationUseCaseImpl @Inject constructor(
    private val repository: LocalDataAccessRepository
) : ObserveLocalDataGenerationUseCase {
    override fun invoke(): StateFlow<Long> = repository.generation
}
