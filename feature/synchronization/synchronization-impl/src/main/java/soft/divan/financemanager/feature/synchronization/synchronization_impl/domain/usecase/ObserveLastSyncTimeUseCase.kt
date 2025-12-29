package soft.divan.financemanager.feature.synchronization.synchronization_impl.domain.usecase

import kotlinx.coroutines.flow.Flow

interface ObserveLastSyncTimeUseCase {
    operator fun invoke(): Flow<Long?>
}