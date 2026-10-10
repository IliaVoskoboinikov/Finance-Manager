package soft.divan.financemanager.core.domain.usecase

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.core.domain.result.DomainResult

/**
 * Сколько локальных изменений так и не доехало до сервера.
 *
 * Ноль — обычное состояние; всё, что больше, стоит показать пользователю: данные сохранены на
 * устройстве, но на других устройствах их нет.
 */
interface ObserveUnsentOperationsUseCase {
    operator fun invoke(): Flow<DomainResult<Int>>
}
