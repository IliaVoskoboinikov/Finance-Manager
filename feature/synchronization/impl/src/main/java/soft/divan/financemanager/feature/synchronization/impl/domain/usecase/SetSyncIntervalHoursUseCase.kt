package soft.divan.financemanager.feature.synchronization.impl.domain.usecase

interface SetSyncIntervalHoursUseCase {
    suspend operator fun invoke(hours: Int)
}