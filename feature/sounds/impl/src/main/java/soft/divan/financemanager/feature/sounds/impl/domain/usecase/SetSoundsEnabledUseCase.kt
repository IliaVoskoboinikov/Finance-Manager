package soft.divan.financemanager.feature.sounds.impl.domain.usecase

interface SetSoundsEnabledUseCase {
    suspend operator fun invoke(isEnabled: Boolean)
}
