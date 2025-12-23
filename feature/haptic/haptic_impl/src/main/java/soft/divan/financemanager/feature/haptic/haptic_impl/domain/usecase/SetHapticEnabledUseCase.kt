package soft.divan.financemanager.feature.haptic.haptic_impl.domain.usecase

interface SetHapticEnabledUseCase {
    suspend operator fun invoke(isEnabled: Boolean)
}