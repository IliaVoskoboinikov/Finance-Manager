package soft.divan.financemanager.sync.domain.usecase

interface SetSyncIntervalHoursUseCase {
    suspend operator fun invoke(hours: Int)
}