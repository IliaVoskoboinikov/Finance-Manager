package soft.divan.financemanager.feature.security.impl.presenter.screen

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import soft.divan.financemanager.feature.security.impl.domain.model.PinLockStatus
import soft.divan.financemanager.feature.security.impl.presenter.model.PinLockError
import soft.divan.financemanager.feature.security.impl.presenter.model.PinLockUiState
import soft.divan.financemanager.uikit.theme.FinanceManagerTheme

/**
 * Превью экрана замка для галереи `@Preview`.
 *
 * Живёт отдельно от `PinLockScreen.kt`: там экран тянет `hiltViewModel()` и `BiometricPrompt`, а
 * превью обязано рисоваться без DI. Здесь — только [PinLockContent] с подставленным состоянием.
 */
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun PreviewPinLockScreen() {
    FinanceManagerTheme {
        PinLockContent(
            uiState = PinLockUiState(biometricEnabled = true),
            lockoutSecondsLeft = 0,
            showBiometricButton = true,
            actions = PinLockActions.NONE
        )
    }
}

@Preview(name = "Wrong PIN — dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewPinLockScreenWrongPin() {
    FinanceManagerTheme(darkTheme = true) {
        PinLockContent(
            uiState = PinLockUiState(
                status = PinLockStatus(attemptsLeft = 3),
                error = PinLockError.WrongPin(attemptsLeft = 3),
                dataProtectedByPin = true
            ),
            lockoutSecondsLeft = 0,
            showBiometricButton = false,
            actions = PinLockActions.NONE
        )
    }
}

@Preview(name = "Locked out — large font", showBackground = true, fontScale = 1.5f)
@Composable
fun PreviewPinLockScreenLockedOut() {
    FinanceManagerTheme {
        PinLockContent(
            uiState = PinLockUiState(
                status = PinLockStatus(attemptsLeft = 4),
                error = PinLockError.LockedOut,
                dataProtectedByPin = true
            ),
            lockoutSecondsLeft = 272,
            showBiometricButton = true,
            actions = PinLockActions.NONE
        )
    }
}

@Preview(name = "Last attempt — dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewPinLockScreenLastAttempt() {
    FinanceManagerTheme(darkTheme = true) {
        PinLockContent(
            uiState = PinLockUiState(
                status = PinLockStatus(attemptsLeft = 1),
                dataProtectedByPin = true
            ),
            lockoutSecondsLeft = 0,
            showBiometricButton = false,
            actions = PinLockActions.NONE
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewForgotPinDialogGuest() {
    FinanceManagerTheme {
        ForgotPinDialog(isGuest = true, onConfirm = {}, onDismiss = {})
    }
}
