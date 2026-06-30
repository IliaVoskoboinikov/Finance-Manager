package soft.divan.financemanager.core.auth.data.source

import kotlinx.coroutines.flow.Flow

interface TokenLocalDataSource {
    fun getAccessToken(): Flow<String?>
    suspend fun updateAccessToken(token: String?)
    fun getRefreshToken(): Flow<String?>
    suspend fun updateRefreshToken(token: String?)
    suspend fun clearTokens()
}
