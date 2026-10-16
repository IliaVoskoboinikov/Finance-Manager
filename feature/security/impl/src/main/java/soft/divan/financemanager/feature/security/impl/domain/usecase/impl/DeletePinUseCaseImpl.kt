package soft.divan.financemanager.feature.security.impl.domain.usecase.impl

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import soft.divan.common.di.IoDispatcher
import soft.divan.financemanager.core.domain.model.DataProtectionLevel
import soft.divan.financemanager.core.domain.repository.DataProtectionRepository
import soft.divan.financemanager.feature.security.impl.domain.model.DeletePinResult
import soft.divan.financemanager.feature.security.impl.domain.repository.SecurityRepository
import soft.divan.financemanager.feature.security.impl.domain.usecase.DeletePinUseCase
import javax.inject.Inject

class DeletePinUseCaseImpl @Inject constructor(
    private val protection: DataProtectionRepository,
    private val repository: SecurityRepository,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : DeletePinUseCase {

    override suspend fun invoke(pin: String): DeletePinResult = when {
        protection.currentLevel() == DataProtectionLevel.PIN -> DeletePinResult.NOT_ALLOWED

        !withContext(ioDispatcher) { repository.verifyPin(pin) } -> DeletePinResult.WRONG_PIN

        else -> {
            withContext(ioDispatcher) { repository.deletePin() }
            DeletePinResult.DELETED
        }
    }
}
