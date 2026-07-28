package soft.divan.financemanager.core.auth.data.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import soft.divan.financemanager.core.auth.data.dto.AuthResponseDto
import soft.divan.financemanager.core.auth.data.dto.RefreshRequestDto
import soft.divan.financemanager.core.auth.data.dto.UserCredentialsDto
import soft.divan.financemanager.core.auth.data.dto.YandexAuthRequestDto

class AuthDataModelsTest {

    @Test
    fun `NetworkConstants define bearer auth header`() {
        assertThat(NetworkConstants.AUTH_HEADER).isEqualTo("Authorization")
        assertThat(NetworkConstants.BEARER_PREFIX).isEqualTo("Bearer ")
    }

    @Test
    fun `AuthResponseDto allows null tokens per server contract`() {
        val dto = AuthResponseDto(accessToken = null, refreshToken = null)

        assertThat(dto.accessToken).isNull()
        assertThat(dto.refreshToken).isNull()
    }

    @Test
    fun `AuthResponseDto equality is structural`() {
        assertThat(AuthResponseDto("a", "r")).isEqualTo(AuthResponseDto("a", "r"))
        assertThat(AuthResponseDto("a", "r")).isNotEqualTo(AuthResponseDto("a", "x"))
    }

    @Test
    fun `RefreshRequestDto carries refresh token`() {
        assertThat(RefreshRequestDto("refresh").refreshToken).isEqualTo("refresh")
        assertThat(RefreshRequestDto(null).refreshToken).isNull()
    }

    @Test
    fun `UserCredentialsDto carries name and password`() {
        val dto = UserCredentialsDto(name = "user", password = "secret")

        assertThat(dto.name).isEqualTo("user")
        assertThat(dto.password).isEqualTo("secret")
    }

    @Test
    fun `YandexAuthRequestDto carries access token`() {
        assertThat(YandexAuthRequestDto(accessToken = "ya-token").accessToken).isEqualTo("ya-token")
    }
}
