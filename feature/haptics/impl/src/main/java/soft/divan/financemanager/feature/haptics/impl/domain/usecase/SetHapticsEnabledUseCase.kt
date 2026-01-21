package soft.divan.financemanager.feature.haptics.impl.domain.usecase

interface SetHapticsEnabledUseCase {
    suspend operator fun invoke(isEnabled: Boolean)
}