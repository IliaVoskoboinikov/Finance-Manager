package soft.divan.financemanager.feature.security.impl.domain.usecase.impl

import soft.divan.financemanager.feature.security.impl.domain.repository.SecurityRepository
import soft.divan.financemanager.feature.security.impl.domain.usecase.IsPinSetUseCase
import javax.inject.Inject

class IsPinSetUseCaseImpl @Inject constructor(
    val repository: SecurityRepository
) : IsPinSetUseCase {
    override fun invoke(): Boolean {
        return repository.isPinSet()
    }
}
