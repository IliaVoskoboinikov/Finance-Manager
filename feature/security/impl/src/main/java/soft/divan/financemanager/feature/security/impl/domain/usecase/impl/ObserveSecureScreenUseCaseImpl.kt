package soft.divan.financemanager.feature.security.impl.domain.usecase.impl

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.feature.security.impl.domain.repository.SecureScreenRepository
import soft.divan.financemanager.feature.security.impl.domain.usecase.ObserveSecureScreenUseCase
import javax.inject.Inject

class ObserveSecureScreenUseCaseImpl @Inject constructor(
    private val repository: SecureScreenRepository
) : ObserveSecureScreenUseCase {
    override fun invoke(): Flow<Boolean> = repository.observeEnabled()
}
