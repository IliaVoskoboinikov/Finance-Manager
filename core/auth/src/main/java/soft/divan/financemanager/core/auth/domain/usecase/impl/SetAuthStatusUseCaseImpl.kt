package soft.divan.financemanager.core.auth.domain.usecase.impl

import soft.divan.financemanager.core.auth.data.source.SessionLocalDataSource
import soft.divan.financemanager.core.auth.domain.model.AuthStatus
import soft.divan.financemanager.core.auth.domain.usecase.SetAuthStatusUseCase
import javax.inject.Inject

class SetAuthStatusUseCaseImpl @Inject constructor(
    private val sessionLocalDataSource: SessionLocalDataSource
) : SetAuthStatusUseCase {
    override suspend fun invoke(authStatus: AuthStatus) {
        return sessionLocalDataSource.setAuthStatus(authStatus)
    }
}
