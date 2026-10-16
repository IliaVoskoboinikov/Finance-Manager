package soft.divan.financemanager.feature.security.impl.presenter.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import soft.divan.financemanager.feature.security.impl.domain.model.PinLockStatus

class PinLockUiStateTest {

    @Test
    fun `countdown wins over any error`() {
        val state = PinLockUiState(
            status = PinLockStatus(attemptsLeft = 1),
            error = PinLockError.WrongPin(attemptsLeft = 1)
        )

        assertThat(state.message(lockoutSecondsLeft = 42)).isEqualTo(PinLockMessage.LockedOut(42))
    }

    @Test
    fun `wrong pin reports the attempts left`() {
        val state = PinLockUiState(
            status = PinLockStatus(attemptsLeft = 3),
            error = PinLockError.WrongPin(attemptsLeft = 3)
        )

        assertThat(state.message(0)).isEqualTo(PinLockMessage.WrongPin(3))
    }

    @Test
    fun `wrong pin of the interface lock has no limit`() {
        val state = PinLockUiState(error = PinLockError.WrongPin(attemptsLeft = null))

        assertThat(state.message(0)).isEqualTo(PinLockMessage.WrongPin(null))
    }

    @Test
    fun `last attempt is announced after the pause of the ninth failure`() {
        // Так состояние выглядит, когда пауза кончилась: ошибка сброшена, учёт попыток остался
        val state = PinLockUiState(status = PinLockStatus(attemptsLeft = 1), error = null)

        assertThat(state.message(0)).isEqualTo(PinLockMessage.LastAttempt)
    }

    @Test
    fun `last attempt is announced when the lockout error outlived its countdown`() {
        val state = PinLockUiState(
            status = PinLockStatus(attemptsLeft = 1),
            error = PinLockError.LockedOut
        )

        assertThat(state.message(0)).isEqualTo(PinLockMessage.LastAttempt)
    }

    @Test
    fun `nothing is shown while attempts are plenty`() {
        val limited = PinLockUiState(status = PinLockStatus(attemptsLeft = 6))
        val unlimited = PinLockUiState()

        assertThat(limited.message(0)).isEqualTo(PinLockMessage.None)
        assertThat(unlimited.message(0)).isEqualTo(PinLockMessage.None)
    }

    @Test
    fun `invalidated biometrics are explained`() {
        val state = PinLockUiState(
            status = PinLockStatus(attemptsLeft = 1),
            error = PinLockError.BiometricInvalidated
        )

        assertThat(state.message(0)).isEqualTo(PinLockMessage.BiometricInvalidated)
    }
}
