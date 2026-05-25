package soft.divan.financemanager.core.auth.domain.usecase

import soft.divan.financemanager.core.auth.domain.model.AuthStatus

interface SetAuthStatusUseCase {
    suspend operator fun invoke(authStatus: AuthStatus)
}
