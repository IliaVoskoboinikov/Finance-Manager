package soft.divan.financemanager.sync.domain.usecase

import kotlinx.coroutines.flow.Flow

interface ObserveLastSyncTimeUseCase {
    operator fun invoke(): Flow<Long?>
}
// Revue me>>
