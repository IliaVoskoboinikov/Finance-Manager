package soft.divan.financemanager.feature.security.impl.domain.usecase

import kotlinx.coroutines.flow.Flow

/**
 * Прятать ли содержимое приложения (`FLAG_SECURE`): запрет скриншотов и пустое превью в списке
 * недавних. Включено по умолчанию.
 */
interface ObserveSecureScreenUseCase {
    operator fun invoke(): Flow<Boolean>
}
