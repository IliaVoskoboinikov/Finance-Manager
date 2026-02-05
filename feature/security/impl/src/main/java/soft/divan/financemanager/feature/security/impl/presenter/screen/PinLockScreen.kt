package soft.divan.financemanager.feature.security.impl.presenter.screen

import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import soft.divan.financemanager.feature.security.impl.R
import soft.divan.financemanager.feature.security.impl.presenter.model.SecurityUiState
import soft.divan.financemanager.feature.security.impl.presenter.viewmodel.SecurityViewModel
import soft.divan.financemanager.uikit.components.ErrorContent
import soft.divan.financemanager.uikit.components.LoadingProgressBar

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun PreviewPinLockScreen() {
    PinLockScreenContent(pin = "1234", onPinCorrect = { }, null)
}

@Composable
fun PinLockScreen(
    onPinCorrect: () -> Unit,
    viewModel: SecurityViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val authenticationCallback =
        @RequiresApi(Build.VERSION_CODES.P)
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                ""
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onPinCorrect()
            }

            override fun onAuthenticationFailed() {
                ""
            }
        }

    when (uiState) {
        is SecurityUiState.Error -> ErrorContent(onClick = {})

        is SecurityUiState.Loading -> LoadingProgressBar()

        is SecurityUiState.Success -> PinLockScreenContent(
            pin = (uiState as SecurityUiState.Success).pin,
            onPinCorrect = onPinCorrect,
            authenticationCallback = authenticationCallback
        )
    }
}

@Composable
fun PinLockScreenContent(
    pin: String,
    onPinCorrect: () -> Unit,
    authenticationCallback: BiometricPrompt.AuthenticationCallback? = null
) {
    var errorMessage by remember { mutableStateOf("") }
    PinEntryCommonScreen(
        titleId = R.string.input_password,
        errorMessage = errorMessage,
        showBiometricButton = true,
        onPinEntered = { enteredPin ->
            if (enteredPin == pin) {
                errorMessage = ""
                onPinCorrect()
            } else {
                errorMessage = "Неверный PIN, попробуйте снова"
            }
        },
        authenticationCallback = authenticationCallback
    )
}
