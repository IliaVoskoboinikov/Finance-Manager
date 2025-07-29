package soft.divan.financemanager.feature.security.security_impl.domain.usecase.impl

import soft.divan.financemanager.feature.security.security_impl.domain.repository.SecurityRepository
import soft.divan.financemanager.feature.security.security_impl.domain.usecase.SavePinUseCase
import javax.inject.Inject

class SavePinUseCaseImpl @Inject constructor(
    val repository: SecurityRepository
) : SavePinUseCase {
    override fun invoke(pin: String) {
        repository.savePin(pin)
    }
}