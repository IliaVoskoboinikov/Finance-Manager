package soft.divan.financemanager.feature.security.impl.domain.usecase.impl

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import soft.divan.common.di.IoDispatcher
import soft.divan.financemanager.core.domain.model.DataProtectionLevel
import soft.divan.financemanager.core.domain.model.ProtectionChangeResult
import soft.divan.financemanager.core.domain.repository.DataProtectionRepository
import soft.divan.financemanager.feature.security.impl.domain.repository.SecurityRepository
import soft.divan.financemanager.feature.security.impl.domain.usecase.ChangePinUseCase
import javax.inject.Inject

class ChangePinUseCaseImpl @Inject constructor(
    private val protection: DataProtectionRepository,
    private val securityRepository: SecurityRepository,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ChangePinUseCase {

    /**
     * На уровне PIN текущий PIN проверяет хранилище ключей, ниже — хеш. Новый хеш пишется только
     * после того, как ключ перезавёрнут: сбой посередине оставит хеш старым, а его перезапишет
     * следующий выход с уровня PIN.
     */
    override suspend fun invoke(currentPin: String, newPin: String): ProtectionChangeResult {
        val pinLevel = protection.currentLevel() == DataProtectionLevel.PIN
        if (!pinLevel && !withContext(ioDispatcher) { securityRepository.verifyPin(currentPin) }) {
            return ProtectionChangeResult.WrongPin
        }
        val result = protection.changePin(currentPin, newPin)
        if (result == ProtectionChangeResult.Changed) {
            withContext(ioDispatcher) { securityRepository.savePin(newPin) }
        }
        return result
    }
}
