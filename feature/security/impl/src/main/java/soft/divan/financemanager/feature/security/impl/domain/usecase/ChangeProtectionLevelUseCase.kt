package soft.divan.financemanager.feature.security.impl.domain.usecase

import soft.divan.financemanager.core.domain.model.DataProtectionLevel
import soft.divan.financemanager.core.domain.model.ProtectionChangeResult

/**
 * Смена уровня защиты данных.
 *
 * [pin] обязателен для входа на уровень PIN и выхода с него; это PIN приложения — тот же, что
 * открывает замок. [isNewPin] — PIN только что придуман (раньше его не было): он сохраняется
 * вместе со сменой уровня, а если уровень включить не удалось — не остаётся.
 */
interface ChangeProtectionLevelUseCase {
    suspend operator fun invoke(
        target: DataProtectionLevel,
        pin: String?,
        isNewPin: Boolean = false
    ): ProtectionChangeResult
}
