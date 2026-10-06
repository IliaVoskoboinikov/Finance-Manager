package soft.divan.financemanager.feature.security.impl.domain.usecase.impl

import soft.divan.financemanager.feature.security.impl.domain.repository.SecureScreenRepository
import soft.divan.financemanager.feature.security.impl.domain.usecase.SetSecureScreenUseCase
import javax.inject.Inject

class SetSecureScreenUseCaseImpl @Inject constructor(
    private val repository: SecureScreenRepository
) : SetSecureScreenUseCase {
    override suspend fun invoke(enabled: Boolean) = repository.setEnabled(enabled)
}
