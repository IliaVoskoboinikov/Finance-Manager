package soft.divan.financemanager.core.domain.usecase

import kotlinx.coroutines.flow.StateFlow

/**
 * Поколение локальных данных: растёт при каждом стирании. Нужно тем, кто держит состояние прежних
 * данных дольше, чем они живут, — например, сохранённой под замком навигации.
 */
interface ObserveLocalDataGenerationUseCase {
    operator fun invoke(): StateFlow<Long>
}
