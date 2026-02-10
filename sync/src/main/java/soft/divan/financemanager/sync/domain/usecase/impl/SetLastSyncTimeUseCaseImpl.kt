package soft.divan.financemanager.sync.domain.usecase.impl

import soft.divan.financemanager.sync.domain.repository.SyncRepository
import soft.divan.financemanager.sync.domain.usecase.SetLastSyncTimeUseCase
import javax.inject.Inject

class SetLastSyncTimeUseCaseImpl @Inject constructor(
    private val repository: SyncRepository
) : SetLastSyncTimeUseCase {
    override suspend fun invoke(timeMillis: Long) {
        repository.setLastSyncTime(timeMillis)
    }
}
// Revue me>>
