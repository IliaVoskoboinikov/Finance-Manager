package soft.divan.financemanager.core.auth.domain.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class SessionStateTest {

    @Test
    fun `Unauthorized maps to UNAUTHORIZED status`() {
        assertThat(SessionState.Unauthorized.status).isEqualTo(AuthStatus.UNAUTHORIZED)
    }

    @Test
    fun `Guest maps to GUEST status`() {
        assertThat(SessionState.Guest.status).isEqualTo(AuthStatus.GUEST)
    }

    @Test
    fun `Authorized maps to AUTHORIZED status and carries tokens`() {
        val state = SessionState.Authorized(accessToken = "access", refreshToken = "refresh")

        assertThat(state.status).isEqualTo(AuthStatus.AUTHORIZED)
        assertThat(state.accessToken).isEqualTo("access")
        assertThat(state.refreshToken).isEqualTo("refresh")
    }

    @Test
    fun `Authorized equality is structural`() {
        assertThat(SessionState.Authorized("a", "r"))
            .isEqualTo(SessionState.Authorized("a", "r"))
        assertThat(SessionState.Authorized("a", "r"))
            .isNotEqualTo(SessionState.Authorized("a", "other"))
    }

    @Test
    fun `AuthStatus contains all session kinds`() {
        assertThat(AuthStatus.entries).containsExactly(
            AuthStatus.GUEST,
            AuthStatus.AUTHORIZED,
            AuthStatus.UNAUTHORIZED
        )
    }

    @Test
    fun `OnLoginSuccess event carries tokens and merge flag`() {
        val event = AuthEvent.OnLoginSuccess(
            accessToken = "access",
            refreshToken = "refresh",
            shouldMergeData = true
        )

        assertThat(event.accessToken).isEqualTo("access")
        assertThat(event.refreshToken).isEqualTo("refresh")
        assertThat(event.shouldMergeData).isTrue()
    }

    @Test
    fun `OnLogout event carries clear-data flag`() {
        assertThat(AuthEvent.OnLogout(shouldClearData = true).shouldClearData).isTrue()
        assertThat(AuthEvent.OnLogout(shouldClearData = false).shouldClearData).isFalse()
    }

    @Test
    fun `singleton events are distinct`() {
        val events: Set<AuthEvent> = setOf(
            AuthEvent.OnSessionExpired,
            AuthEvent.OnEnterAsGuest,
            AuthEvent.OnClearData
        )

        assertThat(events).hasSize(3)
    }
}
