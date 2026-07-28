package soft.divan.financemanager.core.auth.data.dto

import com.google.gson.annotations.SerializedName

/**
 * Тело запроса на обмен OAuth-токена Яндекса на внутреннюю пару токенов приложения.
 *
 * [accessToken] — access_token, полученный на устройстве от Яндекса через Yandex ID SDK.
 * Бэкенд самостоятельно проверяет его в Яндексе и при успехе выдаёт свою пару
 * accessToken/refreshToken (см. [AuthResponseDto]).
 */
data class YandexAuthRequestDto(
    @SerializedName("accessToken")
    val accessToken: String
)
