package soft.divan.financemanager.feature.security.impl.domain.usecase.impl

import soft.divan.financemanager.feature.security.impl.domain.repository.SecurityRepository
import soft.divan.financemanager.feature.security.impl.domain.usecase.GetSavedPinUseCase
import javax.inject.Inject

class GetSavedPinUseCaseImpl @Inject constructor(
    val repository: SecurityRepository
) : GetSavedPinUseCase {
    override fun invoke(): String? {
        return repository.getPin()
    }
}
