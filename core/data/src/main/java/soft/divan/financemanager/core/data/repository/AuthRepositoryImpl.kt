package soft.divan.financemanager.core.data.repository

import soft.divan.financemanager.core.auth.data.api.AuthApiService
import soft.divan.financemanager.core.auth.data.dto.UserCredentialsDto
import soft.divan.financemanager.core.auth.domain.model.AuthEvent
import soft.divan.financemanager.core.auth.domain.provider.AuthStateProvider
import soft.divan.financemanager.core.data.util.safeCall.safeApiCall
import soft.divan.financemanager.core.domain.repository.AuthRepository
import soft.divan.financemanager.core.domain.result.DomainResult
import soft.divan.financemanager.core.loggingerror.ErrorLogger
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApiService,
    private val authStateProvider: AuthStateProvider,
    private val errorLogger: ErrorLogger
) : AuthRepository {

    override suspend fun login(
        name: String,
        password: String,
        shouldMergeData: Boolean
    ): DomainResult<Unit> {
        val result = safeApiCall(errorLogger) {
            authApi.login(UserCredentialsDto(name, password))
        }

        return when (result) {
            is DomainResult.Success -> {
                val response = result.data
                authStateProvider.sendEvent(
                    AuthEvent.OnLoginSuccess(
                        accessToken = response.accessToken ?: "",
                        refreshToken = response.refreshToken ?: "",
                        shouldMergeData = shouldMergeData
                    )
                )
                DomainResult.Success(Unit)
            }

            is DomainResult.Failure -> {
                DomainResult.Failure(result.error)
            }
        }
    }

    override suspend fun register(
        name: String,
        password: String,
        shouldMergeData: Boolean
    ): DomainResult<Unit> {
        val result = safeApiCall(errorLogger) {
            authApi.register(UserCredentialsDto(name, password))
        }

        return when (result) {
            is DomainResult.Success -> {
                val response = result.data
                authStateProvider.sendEvent(
                    AuthEvent.OnLoginSuccess(
                        accessToken = response.accessToken ?: "",
                        refreshToken = response.refreshToken ?: "",
                        shouldMergeData = shouldMergeData
                    )
                )
                DomainResult.Success(Unit)
            }

            is DomainResult.Failure -> {
                DomainResult.Failure(result.error)
            }
        }
    }

    override suspend fun logout(shouldClearData: Boolean): DomainResult<Unit> {
        safeApiCall(errorLogger) {
            authApi.logout()
        }
        authStateProvider.sendEvent(AuthEvent.OnLogout(shouldClearData))
        return DomainResult.Success(Unit)
    }

    override suspend fun loginAsGuest(): DomainResult<Unit> {
        authStateProvider.sendEvent(AuthEvent.OnEnterAsGuest)
        return DomainResult.Success(Unit)
    }

    override suspend fun clearUserData(): DomainResult<Unit> {
        authStateProvider.sendEvent(AuthEvent.OnClearData)
        return DomainResult.Success(Unit)
    }
}
