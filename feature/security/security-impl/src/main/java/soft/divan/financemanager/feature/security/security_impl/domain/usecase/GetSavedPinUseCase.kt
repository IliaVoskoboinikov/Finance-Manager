package soft.divan.financemanager.feature.security.security_impl.domain.usecase

interface GetSavedPinUseCase {
    operator fun invoke(): String?
}