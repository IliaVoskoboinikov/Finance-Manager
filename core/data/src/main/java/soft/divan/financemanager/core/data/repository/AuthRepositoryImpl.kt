package soft.divan.financemanager.core.data.repository

import retrofit2.Response
import soft.divan.financemanager.core.auth.data.api.AuthApiService
import soft.divan.financemanager.core.auth.data.dto.AuthResponseDto
import soft.divan.financemanager.core.auth.data.dto.UserCredentialsDto
import soft.divan.financemanager.core.auth.data.dto.YandexAuthRequestDto
import soft.divan.financemanager.core.auth.domain.model.AuthEvent
import soft.divan.financemanager.core.auth.domain.provider.AuthStateProvider
import soft.divan.financemanager.core.data.error.DataError
import soft.divan.financemanager.core.data.mapper.toDomainError
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
    ): DomainResult<Unit> = authenticate(shouldMergeData) {
        authApi.login(UserCredentialsDto(name, password))
    }

    override suspend fun register(
        name: String,
        password: String,
        shouldMergeData: Boolean
    ): DomainResult<Unit> = authenticate(shouldMergeData) {
        authApi.register(UserCredentialsDto(name, password))
    }

    override suspend fun loginWithYandex(
        accessToken: String,
        shouldMergeData: Boolean
    ): DomainResult<Unit> = authenticate(shouldMergeData) {
        authApi.oauthYandex(YandexAuthRequestDto(accessToken))
    }

    /**
     * Общая логика входа/регистрации.
     *
     * Сервер по контракту (Swagger) может вернуть 200 с null-токенами
     * (accessToken/refreshToken помечены nullable). Такой ответ НЕ является валидной сессией:
     * с пустым токеном [soft.divan.financemanager.core.network.interceptor.AuthInterceptor]
     * не добавит заголовок Authorization, все запросы будут отклоняться (401),
     * а пользователь останется в статусе AUTHORIZED без возможности нормально работать.
     * Поэтому пустые/отсутствующие токены трактуются как ошибка авторизации.
     */
    private suspend fun authenticate(
        shouldMergeData: Boolean,
        apiCall: suspend () -> Response<AuthResponseDto>
    ): DomainResult<Unit> {
        return when (val result = safeApiCall(errorLogger) { apiCall() }) {
            is DomainResult.Success -> {
                val accessToken = result.data.accessToken
                val refreshToken = result.data.refreshToken

                if (accessToken.isNullOrBlank() || refreshToken.isNullOrBlank()) {
                    errorLogger.recordError("Auth succeeded (200) but tokens are null/blank")
                    DomainResult.Failure(
                        DataError.Unknown(
                            IllegalStateException("Auth response contained empty tokens")
                        ).toDomainError()
                    )
                } else {
                    authStateProvider.sendEvent(
                        AuthEvent.OnLoginSuccess(
                            accessToken = accessToken,
                            refreshToken = refreshToken,
                            shouldMergeData = shouldMergeData
                        )
                    )
                    DomainResult.Success(Unit)
                }
            }

            is DomainResult.Failure -> DomainResult.Failure(result.error)
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
