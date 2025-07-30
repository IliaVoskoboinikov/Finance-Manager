package soft.divan.financemanager.feature.security.security_impl.presenter.screen

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
import soft.divan.financemanager.feature.security.security_impl.R
import soft.divan.financemanager.feature.security.security_impl.presenter.viewmodel.SecurityViewModel


@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun PreviewPinLockScreen() {
    PinLockScreen({})
}

@Composable
fun PinLockScreen(onPinCorrect: () -> Unit, viewModel: SecurityViewModel = hiltViewModel()) {
    var errorMessage by remember { mutableStateOf("") }

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

    val password = viewModel.pin.collectAsStateWithLifecycle().value
    PinEntryCommonScreen(
        titleId = R.string.input_password,
        errorMessage = errorMessage,
        showBiometricButton = true,
        onPinEntered = { enteredPin ->
            if (enteredPin == password) {
                errorMessage = ""
                onPinCorrect()
            } else {
                errorMessage = "Неверный PIN, попробуйте снова"
            }
        },
        authenticationCallback = authenticationCallback
    )
}

