package soft.divan.financemanager.core.domain.usecase.impl

import soft.divan.financemanager.core.domain.repository.LocalDataAccessRepository
import soft.divan.financemanager.core.domain.usecase.LockLocalDataUseCase
import javax.inject.Inject

class LockLocalDataUseCaseImpl @Inject constructor(
    private val repository: LocalDataAccessRepository
) : LockLocalDataUseCase {
    override suspend fun invoke() = repository.lock()
}
