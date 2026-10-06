package soft.divan.financemanager.feature.security.impl.domain.usecase.impl

import soft.divan.financemanager.core.domain.model.DataProtectionLevel
import soft.divan.financemanager.core.domain.repository.DataProtectionRepository
import soft.divan.financemanager.core.domain.repository.LocalDataAccessRepository
import soft.divan.financemanager.feature.security.impl.domain.model.PinLockStatus
import soft.divan.financemanager.feature.security.impl.domain.usecase.GetPinLockStatusUseCase
import javax.inject.Inject

class GetPinLockStatusUseCaseImpl @Inject constructor(
    private val protection: DataProtectionRepository,
    private val localData: LocalDataAccessRepository
) : GetPinLockStatusUseCase {

    override suspend fun invoke(): PinLockStatus =
        if (protection.currentLevel() == DataProtectionLevel.PIN) {
            localData.pinLockout().toStatus()
        } else {
            PinLockStatus.UNLIMITED
        }
}
