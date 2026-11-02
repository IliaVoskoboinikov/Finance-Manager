package soft.divan.financemanager.feature.security.impl.domain.usecase

import soft.divan.financemanager.feature.security.impl.domain.model.PinCheckResult

/**
 * Проверка PIN на экране замка.
 *
 * На уровне PIN проверка — это разворачивание ключа данных с учётом попыток (паузы, стирание на
 * десятой ошибке); ниже — сверка с хешем, это только замок интерфейса.
 */
interface UnlockWithPinUseCase {
    suspend operator fun invoke(pin: String): PinCheckResult
}
