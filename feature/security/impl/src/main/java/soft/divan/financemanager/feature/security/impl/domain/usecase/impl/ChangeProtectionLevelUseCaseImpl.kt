package soft.divan.financemanager.feature.security.impl.domain.usecase.impl

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import soft.divan.common.di.IoDispatcher
import soft.divan.financemanager.core.domain.model.DataProtectionLevel
import soft.divan.financemanager.core.domain.model.ProtectionChangeResult
import soft.divan.financemanager.core.domain.repository.DataProtectionRepository
import soft.divan.financemanager.feature.security.impl.domain.repository.SecurityRepository
import soft.divan.financemanager.feature.security.impl.domain.usecase.ChangeProtectionLevelUseCase
import javax.inject.Inject

class ChangeProtectionLevelUseCaseImpl @Inject constructor(
    private val protection: DataProtectionRepository,
    private val securityRepository: SecurityRepository,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ChangeProtectionLevelUseCase {

    /**
     * Входя на уровень PIN, ключ заворачивается тем PIN, что открывает замок, — поэтому он
     * сверяется с хешем: иначе замок и данные оказались бы под разными PIN.
     *
     * Уходя с уровня PIN, PIN проверяет само хранилище (разворачивая ключ), а хеш
     * перезаписывается этим PIN: ниже уровня PIN замок снова держится на хеше, и расхождение,
     * оставшееся после сбоя при смене PIN, здесь исчезает.
     */
    override suspend fun invoke(
        target: DataProtectionLevel,
        pin: String?,
        isNewPin: Boolean
    ): ProtectionChangeResult {
        val current = protection.currentLevel()
        if (current == target) return ProtectionChangeResult.Changed
        if (isNewPin && pin != null) io { securityRepository.savePin(pin) }
        val result = when {
            target == DataProtectionLevel.PIN && !matchesAppPin(pin) -> ProtectionChangeResult.WrongPin
            else -> protection.changeLevel(target, pin)
        }
        val changed = result == ProtectionChangeResult.Changed
        when {
            changed && current == DataProtectionLevel.PIN && pin != null -> io {
                securityRepository.savePin(
                    pin
                )
            }

            // Придуманный ради уровня PIN не должен пережить неудачу: пользователь его не выбирал
            !changed && isNewPin -> io { securityRepository.deletePin() }
        }
        return result
    }

    private suspend fun matchesAppPin(pin: String?): Boolean =
        pin != null && io { securityRepository.verifyPin(pin) }

    private suspend fun <T> io(block: () -> T): T = withContext(ioDispatcher) { block() }
}
