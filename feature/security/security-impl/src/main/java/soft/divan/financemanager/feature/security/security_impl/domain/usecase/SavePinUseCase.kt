package soft.divan.financemanager.feature.security.security_impl.domain.usecase


interface SavePinUseCase {
    operator fun invoke(pin: String)
}