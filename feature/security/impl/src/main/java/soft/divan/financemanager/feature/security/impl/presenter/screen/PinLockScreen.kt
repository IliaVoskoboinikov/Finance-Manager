package soft.divan.financemanager.feature.security.impl.presenter.screen

import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
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
    PinLockScreenContent(onVerifyPin = { it == "1234" }, onPinCorrect = { }, null)
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
                // no-op: пользователь остаётся на экране ввода PIN
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onPinCorrect()
            }

            override fun onAuthenticationFailed() {
                // no-op: неуспешная попытка биометрии — можно повторить или ввести PIN
            }
        }

    when (uiState) {
        is SecurityUiState.Error -> ErrorContent(onClick = {})

        is SecurityUiState.Loading -> LoadingProgressBar()

        is SecurityUiState.Success -> PinLockScreenContent(
            onVerifyPin = viewModel::verifyPin,
            onPinCorrect = onPinCorrect,
            authenticationCallback = authenticationCallback
        )
    }
}

@Composable
fun PinLockScreenContent(
    onVerifyPin: (String) -> Boolean,
    onPinCorrect: () -> Unit,
    authenticationCallback: BiometricPrompt.AuthenticationCallback? = null
) {
    var errorMessage by remember { mutableStateOf("") }
    val wrongPinMessage = stringResource(R.string.wrong_pin)
    PinEntryCommonScreen(
        titleId = R.string.input_password,
        errorMessage = errorMessage,
        showBiometricButton = true,
        onPinEntered = { enteredPin ->
            if (onVerifyPin(enteredPin)) {
                errorMessage = ""
                onPinCorrect()
            } else {
                errorMessage = wrongPinMessage
            }
        },
        authenticationCallback = authenticationCallback
    )
}
