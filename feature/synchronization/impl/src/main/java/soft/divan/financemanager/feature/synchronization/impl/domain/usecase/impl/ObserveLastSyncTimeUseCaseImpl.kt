package soft.divan.financemanager.feature.synchronization.impl.domain.usecase.impl

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.feature.synchronization.impl.domain.usecase.ObserveLastSyncTimeUseCase
import soft.divan.financemanager.sync.domain.repository.SyncRepository
import javax.inject.Inject

class ObserveLastSyncTimeUseCaseImpl @Inject constructor(
    private val repository: SyncRepository,
) : ObserveLastSyncTimeUseCase {
    override fun invoke(): Flow<Long?> = repository.observeLastSyncTime()
}
