package soft.divan.financemanager.sync.domain.usecase

interface SetLastSyncTimeUseCase {
    suspend operator fun invoke(timeMillis: Long)
}