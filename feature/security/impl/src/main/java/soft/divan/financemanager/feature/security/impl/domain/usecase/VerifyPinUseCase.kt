package soft.divan.financemanager.feature.security.impl.domain.usecase

interface VerifyPinUseCase {
    operator fun invoke(pin: String): Boolean
}
