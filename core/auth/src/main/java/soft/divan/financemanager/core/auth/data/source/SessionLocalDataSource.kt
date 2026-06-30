package soft.divan.financemanager.core.auth.data.source

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.core.auth.domain.model.AuthStatus

interface SessionLocalDataSource {
    fun getAuthStatus(): Flow<AuthStatus>
    suspend fun setAuthStatus(status: AuthStatus)
    suspend fun clearSession()
}
