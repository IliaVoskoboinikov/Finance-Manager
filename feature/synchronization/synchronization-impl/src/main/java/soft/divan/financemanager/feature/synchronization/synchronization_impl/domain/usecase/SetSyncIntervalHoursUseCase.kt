package soft.divan.financemanager.feature.synchronization.synchronization_impl.domain.usecase

interface SetSyncIntervalHoursUseCase {
    suspend operator fun invoke(hours: Int)
}