package soft.divan.financemanager.feature.security.impl.domain.usecase

import soft.divan.financemanager.feature.security.impl.domain.model.DeletePinResult

/**
 * Удаление PIN по текущему PIN. На уровне PIN запрещено: им завёрнут ключ данных, и без него
 * данные не открыть — сначала нужно понизить уровень.
 */
interface DeletePinUseCase {
    suspend operator fun invoke(pin: String): DeletePinResult
}
