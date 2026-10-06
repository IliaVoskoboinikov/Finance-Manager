package soft.divan.financemanager.feature.security.impl.domain.usecase.impl

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import soft.divan.common.di.IoDispatcher
import soft.divan.financemanager.core.domain.model.PinLockout
import soft.divan.financemanager.core.domain.model.PinUnlockResult
import soft.divan.financemanager.core.domain.repository.LocalDataAccessRepository
import soft.divan.financemanager.feature.security.impl.domain.model.PinCheckResult
import soft.divan.financemanager.feature.security.impl.domain.model.PinLockStatus
import soft.divan.financemanager.feature.security.impl.domain.repository.SecurityRepository
import soft.divan.financemanager.feature.security.impl.domain.usecase.UnlockWithPinUseCase
import javax.inject.Inject

class UnlockWithPinUseCaseImpl @Inject constructor(
    private val localData: LocalDataAccessRepository,
    private val securityRepository: SecurityRepository,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : UnlockWithPinUseCase {

    override suspend fun invoke(pin: String): PinCheckResult =
        when (val result = localData.unlockWithPin(pin)) {
            PinUnlockResult.Success -> PinCheckResult.Correct

            PinUnlockResult.NotRequired -> checkHash(pin)

            is PinUnlockResult.WrongPin -> PinCheckResult.Wrong(result.lockout.toStatus())

            is PinUnlockResult.LockedOut -> PinCheckResult.LockedOut(result.lockout.toStatus())

            PinUnlockResult.KeyLost -> PinCheckResult.KeyLost

            PinUnlockResult.Wiped -> {
                // PIN забыт или подбирается — оставлять его замком нового пустого профиля нельзя
                withContext(ioDispatcher) { securityRepository.deletePin() }
                PinCheckResult.Wiped
            }
        }

    private suspend fun checkHash(pin: String): PinCheckResult =
        if (withContext(ioDispatcher) { securityRepository.verifyPin(pin) }) {
            PinCheckResult.Correct
        } else {
            PinCheckResult.Wrong(PinLockStatus.UNLIMITED)
        }
}

/** Учёт попыток хранилища ключей → состояние замка для экрана. */
internal fun PinLockout.toStatus() = PinLockStatus(
    attemptsLeft = attemptsLeft,
    lockedUntil = lockedUntil
)
