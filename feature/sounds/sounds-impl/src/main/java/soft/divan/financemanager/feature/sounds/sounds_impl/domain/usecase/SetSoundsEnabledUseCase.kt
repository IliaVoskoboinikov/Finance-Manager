package soft.divan.financemanager.feature.sounds.sounds_impl.domain.usecase

interface SetSoundsEnabledUseCase {
    suspend operator fun invoke(isEnabled: Boolean)
}