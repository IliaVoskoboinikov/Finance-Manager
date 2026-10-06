package soft.divan.financemanager.feature.security.impl.domain.usecase

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.feature.security.impl.domain.model.SecuritySettings

/** Настройки безопасности одним потоком: меняются из разных мест, а экран у них один. */
interface ObserveSecuritySettingsUseCase {
    operator fun invoke(): Flow<SecuritySettings>
}
