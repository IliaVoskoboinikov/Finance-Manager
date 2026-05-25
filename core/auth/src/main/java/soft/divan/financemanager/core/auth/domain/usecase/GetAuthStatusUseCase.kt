package soft.divan.financemanager.core.auth.domain.usecase

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.core.auth.domain.model.AuthStatus

interface GetAuthStatusUseCase {
    operator fun invoke(): Flow<AuthStatus>
}
