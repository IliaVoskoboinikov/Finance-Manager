package soft.divan.financemanager.feature.security.impl.domain.usecase.impl

import soft.divan.financemanager.feature.security.impl.domain.repository.SecurityRepository
import soft.divan.financemanager.feature.security.impl.domain.usecase.VerifyPinUseCase
import javax.inject.Inject

class VerifyPinUseCaseImpl @Inject constructor(
    private val repository: SecurityRepository
) : VerifyPinUseCase {
    override fun invoke(pin: String): Boolean {
        return repository.verifyPin(pin)
    }
}
