package soft.divan.financemanager.feature.auth.impl.presenter.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class AuthActionsSmokeTest {

    @Test
    fun `auth actions defaults are no-op and do not throw`() {
        val actions = AuthActions()

        actions.onUpdateName("user")
        actions.onUpdatePassword("pass")
        actions.onToggleMode()
        actions.onAuthClick()
        actions.onMergeConfirm(true)
        actions.onLogoutClick()
        actions.onLogoutConfirm(false)
        actions.onGuestClick()
        actions.onDismissDialogs()
    }

    @Test
    fun `auth actions invoke provided handlers`() {
        var calls = 0
        val actions = AuthActions(
            onUpdateName = { calls++ },
            onAuthClick = { calls++ },
            onMergeConfirm = { calls++ }
        )

        actions.onUpdateName("user")
        actions.onAuthClick()
        actions.onMergeConfirm(true)

        assertThat(calls).isEqualTo(3)
    }

    @Test
    fun `error state and show-error event carry their payloads`() {
        assertThat(AuthUiState.Error(message = 42).message).isEqualTo(42)
        assertThat(AuthEvent.ShowError("boom").message).isEqualTo("boom")
    }

    @Test
    fun `profile actions defaults are no-op and handlers are invoked`() {
        ProfileActions().onNavigateToAuth()

        var calls = 0
        val actions = ProfileActions(
            onNavigateToAuth = { calls++ },
            onLogoutClick = { calls++ },
            onLogoutConfirm = { calls++ },
            onDismissDialogs = { calls++ }
        )

        actions.onNavigateToAuth()
        actions.onLogoutClick()
        actions.onLogoutConfirm(true)
        actions.onDismissDialogs()

        assertThat(calls).isEqualTo(4)
    }
}
