package soft.divan.financemanager.core.auth.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import soft.divan.financemanager.core.auth.data.dto.AuthResponseDto
import soft.divan.financemanager.core.auth.data.dto.RefreshRequestDto
import soft.divan.financemanager.core.auth.data.dto.UserCredentialsDto
import soft.divan.financemanager.core.auth.data.dto.YandexAuthRequestDto

interface AuthApiService {

    @POST("api/v1/auth/register")
    suspend fun register(
        @Body credentials: UserCredentialsDto
    ): Response<AuthResponseDto>

    @POST("api/v1/auth/login")
    suspend fun login(
        @Body credentials: UserCredentialsDto
    ): Response<AuthResponseDto>

    /**
     * Обмен OAuth access_token Яндекса на внутреннюю пару токенов приложения.
     * Возвращает такую же пару accessToken/refreshToken, как обычный login/register.
     */
    @POST("api/v1/auth/oauth/yandex")
    suspend fun oauthYandex(
        @Body request: YandexAuthRequestDto
    ): Response<AuthResponseDto>

    @POST("api/v1/auth/logout")
    suspend fun logout(): Response<Unit>

    @POST("api/v1/auth/refresh")
    suspend fun refresh(
        @Body request: RefreshRequestDto
    ): Response<AuthResponseDto>
}
