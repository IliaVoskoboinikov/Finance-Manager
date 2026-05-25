package soft.divan.financemanager.core.auth.domain.usecase.impl

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.core.auth.data.source.SessionLocalDataSource
import soft.divan.financemanager.core.auth.domain.model.AuthStatus
import soft.divan.financemanager.core.auth.domain.usecase.GetAuthStatusUseCase
import javax.inject.Inject

class GetAuthStatusUseCaseImpl @Inject constructor(
    private val sessionLocalDataSource: SessionLocalDataSource
) : GetAuthStatusUseCase {
    override fun invoke(): Flow<AuthStatus> {
        return sessionLocalDataSource.getAuthStatus()
    }
}
