package soft.divan.financemanager.core.domain.usecase.impl

import soft.divan.financemanager.core.domain.repository.LocalDataAccessRepository
import soft.divan.financemanager.core.domain.usecase.InitializeLocalDataUseCase
import javax.inject.Inject

class InitializeLocalDataUseCaseImpl @Inject constructor(
    private val repository: LocalDataAccessRepository
) : InitializeLocalDataUseCase {
    override suspend fun invoke() = repository.initialize()
}
