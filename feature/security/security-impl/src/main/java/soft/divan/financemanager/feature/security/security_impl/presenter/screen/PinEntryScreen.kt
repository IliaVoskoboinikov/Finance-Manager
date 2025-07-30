package soft.divan.financemanager.feature.security.security_impl.presenter.screen

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import soft.divan.financemanager.feature.security.security_impl.presenter.components.Keyboard
import soft.divan.financemanager.feature.security.security_impl.presenter.components.PinCodeScreenHeader
import soft.divan.financemanager.feature.security.security_impl.presenter.components.RoundedBoxesRow


@Composable
fun PinEntryScreen(
    title: String,
    pinSize: Int = 4,
    errorMessage: String = "",
    clearPinTrigger: Boolean = false,
    onPinEntered: (String) -> Unit,
    onBackspaceClick: () -> Unit = {},
    onFingerprintClick: () -> Unit = {},
    authenticationCallback: BiometricPrompt.AuthenticationCallback? = null,
) {
    val inputPin = remember { mutableStateListOf<Int>() }
    var showBiometricScreen by remember { mutableStateOf(true) }
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current


    // Сброс PIN по внешнему триггеру
    LaunchedEffect(clearPinTrigger) {
        if (clearPinTrigger) inputPin.clear()
    }


    // Проверка длины и отправка результата
    LaunchedEffect(inputPin.size) {
        if (inputPin.size == pinSize) {
            delay(200) // чтобы пользователь успел увидеть ввод
            onPinEntered(inputPin.joinToString(""))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        PinCodeScreenHeader(text = title)


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



