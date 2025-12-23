package soft.divan.financemanager.feature.haptics.haptics_impl.domain.usecase

interface SetHapticsEnabledUseCase {
    suspend operator fun invoke(isEnabled: Boolean)
}