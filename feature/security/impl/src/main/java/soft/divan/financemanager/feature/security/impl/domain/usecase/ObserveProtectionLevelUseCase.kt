package soft.divan.financemanager.feature.security.impl.domain.usecase

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.core.domain.model.DataProtectionLevel

/** Текущий уровень защиты данных. */
interface ObserveProtectionLevelUseCase {
    operator fun invoke(): Flow<DataProtectionLevel>
}
