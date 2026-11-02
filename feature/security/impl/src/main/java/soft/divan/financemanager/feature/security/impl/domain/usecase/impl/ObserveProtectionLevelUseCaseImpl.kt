package soft.divan.financemanager.feature.security.impl.domain.usecase.impl

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.core.domain.model.DataProtectionLevel
import soft.divan.financemanager.core.domain.repository.DataProtectionRepository
import soft.divan.financemanager.feature.security.impl.domain.usecase.ObserveProtectionLevelUseCase
import javax.inject.Inject

class ObserveProtectionLevelUseCaseImpl @Inject constructor(
    private val protection: DataProtectionRepository
) : ObserveProtectionLevelUseCase {
    override fun invoke(): Flow<DataProtectionLevel> = protection.observeLevel()
}
