package soft.divan.financemanager.core.data.repository

import kotlinx.coroutines.flow.first
import soft.divan.financemanager.core.auth.data.api.AuthApiService
import soft.divan.financemanager.core.auth.data.authenticator.AuthManager
import soft.divan.financemanager.core.auth.data.dto.UserCredentialsDto
import soft.divan.financemanager.core.auth.data.source.SessionLocalDataSource
import soft.divan.financemanager.core.auth.domain.model.AuthStatus
import soft.divan.financemanager.core.data.util.safeCall.safeApiCall
import soft.divan.financemanager.core.database.util.DatabaseCleanupManager
import soft.divan.financemanager.core.domain.repository.AuthRepository
import soft.divan.financemanager.core.domain.result.DomainResult
import soft.divan.financemanager.core.loggingerror.ErrorLogger
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApiService,
    private val authManager: AuthManager,
    private val sessionLocalDataSource: SessionLocalDataSource,
    private val databaseCleanupManager: DatabaseCleanupManager,
    private val errorLogger: ErrorLogger
) : AuthRepository {

    override suspend fun login(
        name: String,
        password: String,
        shouldMergeData: Boolean
    ): DomainResult<Unit> {
        val previousStatus = sessionLocalDataSource.getAuthStatus().first()

        val result = safeApiCall(errorLogger) {
            authApi.login(UserCredentialsDto(name, password))
        }

        return when (result) {
            is DomainResult.Success -> {
                // Если переходим из GUEST и НЕ хотим объединять данные — чистим БД
                if (previousStatus == AuthStatus.GUEST && !shouldMergeData) {
                    databaseCleanupManager.clearUserData()
                }

                val response = result.data
                authManager.updateTokens(response.accessToken, response.refreshToken)
                sessionLocalDataSource.setAuthStatus(AuthStatus.AUTHORIZED)
                DomainResult.Success(Unit)
            }

            is DomainResult.Failure -> DomainResult.Failure(result.error)
        }
    }

    override suspend fun register(
        name: String,
        password: String,
        shouldMergeData: Boolean
    ): DomainResult<Unit> {
        val previousStatus = sessionLocalDataSource.getAuthStatus().first()

        val result = safeApiCall(errorLogger) {
            authApi.register(UserCredentialsDto(name, password))
        }

        return when (result) {
            is DomainResult.Success -> {
                // Если переходим из GUEST и НЕ хотим объединять данные — чистим БД
                if (previousStatus == AuthStatus.GUEST && !shouldMergeData) {
                    databaseCleanupManager.clearUserData()
                }

                val response = result.data
                authManager.updateTokens(response.accessToken, response.refreshToken)
                sessionLocalDataSource.setAuthStatus(AuthStatus.AUTHORIZED)
                DomainResult.Success(Unit)
            }

            is DomainResult.Failure -> DomainResult.Failure(result.error)
        }
    }

    override suspend fun logout(shouldClearData: Boolean): DomainResult<Unit> {
        // Выполняем логаут на сервере (опционально)
        safeApiCall(errorLogger) {
            authApi.logout()
        }

        // Локальная очистка сессии
        authManager.clearTokens()

        if (shouldClearData) {
            databaseCleanupManager.clearUserData()
        }

        // Переводим пользователя в режим гостя
        sessionLocalDataSource.setAuthStatus(AuthStatus.GUEST)

        return DomainResult.Success(Unit)
    }

    override suspend fun loginAsGuest(): DomainResult<Unit> {
        // При входе как гость очищаем токены (на случай если они там были)
        authManager.clearTokens()
        sessionLocalDataSource.setAuthStatus(AuthStatus.GUEST)
        return DomainResult.Success(Unit)
    }

    override suspend fun clearUserData(): DomainResult<Unit> {
        databaseCleanupManager.clearUserData()
        return DomainResult.Success(Unit)
    }
}
