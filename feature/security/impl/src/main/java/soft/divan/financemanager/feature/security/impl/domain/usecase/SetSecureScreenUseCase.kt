package soft.divan.financemanager.feature.security.impl.domain.usecase

/** Включает или выключает защиту экрана (`FLAG_SECURE`). */
interface SetSecureScreenUseCase {
    suspend operator fun invoke(enabled: Boolean)
}
