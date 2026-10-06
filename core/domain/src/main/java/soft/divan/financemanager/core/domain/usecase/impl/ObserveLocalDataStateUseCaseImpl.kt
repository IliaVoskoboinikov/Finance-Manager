package soft.divan.financemanager.core.domain.usecase.impl

import kotlinx.coroutines.flow.StateFlow
import soft.divan.financemanager.core.domain.model.LocalDataState
import soft.divan.financemanager.core.domain.repository.LocalDataAccessRepository
import soft.divan.financemanager.core.domain.usecase.ObserveLocalDataStateUseCase
import javax.inject.Inject

class ObserveLocalDataStateUseCaseImpl @Inject constructor(
    private val repository: LocalDataAccessRepository
) : ObserveLocalDataStateUseCase {
    override fun invoke(): StateFlow<LocalDataState> = repository.state
}
