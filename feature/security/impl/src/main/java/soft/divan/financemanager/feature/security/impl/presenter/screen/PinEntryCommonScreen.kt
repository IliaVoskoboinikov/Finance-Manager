package soft.divan.financemanager.feature.security.impl.presenter.screen

import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import soft.divan.financemanager.feature.security.impl.presenter.components.Keyboard
import soft.divan.financemanager.feature.security.impl.presenter.components.PinCodeScreenHeader
import soft.divan.financemanager.feature.security.impl.presenter.components.RoundedBoxesRow

private const val DEFAULT_PIN_SIZE = 4
private const val PIN_INPUT_CONFIRMATION_DELAY_MS = 200L

@Composable
fun PinEntryCommonScreen(
    titleId: Int,
    pinSize: Int = DEFAULT_PIN_SIZE,
    errorMessage: String = "",
    showBiometricButton: Boolean = false,
    onPinEntered: (String) -> Unit,
    onBackspaceClick: () -> Unit = {},
    onFingerprintClick: () -> Unit = {},
    authenticationCallback: BiometricPrompt.AuthenticationCallback? = null
) {
    val inputPin = remember { mutableStateListOf<Int>() }
    var showBiometricScreen by remember { mutableStateOf(true) }

    // Проверка длины и отправка результата
    LaunchedEffect(inputPin.size) {
        if (inputPin.size == pinSize) {
            delay(PIN_INPUT_CONFIRMATION_DELAY_MS) // чтобы пользователь успел увидеть ввод
            onPinEntered(inputPin.joinToString(""))
            inputPin.clear()
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        PinCodeScreenHeader(text = stringResource(titleId))

        RoundedBoxesRow(
            startQuantity = pinSize,
            quantity = inputPin.size
        )

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(16.dp)
            )
        }

        Keyboard(
            showBiometricButton = showBiometricButton,
            onNumberClick = { number ->
                if (inputPin.size < pinSize) inputPin.add(number.toInt())
            },
            onBackspaceClick = {
                if (inputPin.isNotEmpty()) {
                    inputPin.removeAt(inputPin.lastIndex)
                    onBackspaceClick()
                }
            },
            onFingerprintClick = {
                showBiometricScreen = true
                onFingerprintClick()
            }
        )

        if (showBiometricScreen && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && authenticationCallback != null) {
            BiometricScreen(authenticationCallback = authenticationCallback)
            showBiometricScreen = false
        }
    }
}
// Revue me>>
