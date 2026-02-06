package soft.divan.financemanager.sync.domain.usecase

import kotlinx.coroutines.flow.Flow

interface ObserveSyncIntervalHoursUseCase {
    operator fun invoke(): Flow<Int?>
}
// Revue me>>
