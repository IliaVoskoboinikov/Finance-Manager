package soft.divan.financemanager.feature.security.security_impl.presenter.screen

import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
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
    var clearTrigger by remember { mutableStateOf(false) }

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

    LaunchedEffect(clearTrigger) {
        if (clearTrigger) {
            delay(300)
            clearTrigger = false
        }
    }
    val password = viewModel.pin.collectAsStateWithLifecycle().value
    PinEntryCommonScreen(
        titleId = R.string.input_password,
        errorMessage = errorMessage,
        clearPinTrigger = clearTrigger,
        showBiometricButton = true,
        onPinEntered = { enteredPin ->
            if (enteredPin == password) {
                errorMessage = ""
                onPinCorrect()
            } else {
                errorMessage = "Неверный PIN, попробуйте снова"
                clearTrigger = true // очистим поле
                // откат триггера после задержки, чтобы снова сработал при следующем неверном вводе

            }
        },
        authenticationCallback = authenticationCallback
    )
}

