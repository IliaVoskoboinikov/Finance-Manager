package soft.divan.financemanager.feature.security.impl.domain.usecase

import soft.divan.financemanager.core.domain.model.ProtectionChangeResult

/**
 * Смена PIN по текущему PIN. На уровне PIN перезаворачивает ключ данных — без текущего PIN его
 * не достать, поэтому сменить PIN «вслепую» нельзя.
 */
interface ChangePinUseCase {
    suspend operator fun invoke(currentPin: String, newPin: String): ProtectionChangeResult
}
