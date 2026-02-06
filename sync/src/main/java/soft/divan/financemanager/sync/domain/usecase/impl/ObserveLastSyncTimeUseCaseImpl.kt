package soft.divan.financemanager.sync.domain.usecase.impl

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.sync.domain.repository.SyncRepository
import soft.divan.financemanager.sync.domain.usecase.ObserveLastSyncTimeUseCase
import javax.inject.Inject

class ObserveLastSyncTimeUseCaseImpl @Inject constructor(
    private val repository: SyncRepository
) : ObserveLastSyncTimeUseCase {
    override fun invoke(): Flow<Long?> = repository.observeLastSyncTime()
}
// Revue me>>
