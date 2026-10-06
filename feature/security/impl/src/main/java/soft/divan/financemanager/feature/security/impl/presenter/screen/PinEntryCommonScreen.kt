package soft.divan.financemanager.feature.security.impl.presenter.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import soft.divan.financemanager.feature.security.impl.R
import soft.divan.financemanager.feature.security.impl.presenter.components.Keyboard
import soft.divan.financemanager.feature.security.impl.presenter.components.PinCodeScreenHeader
import soft.divan.financemanager.feature.security.impl.presenter.components.RoundedBoxesRow
import soft.divan.financemanager.uikit.theme.FinanceManagerTheme

private const val DEFAULT_PIN_SIZE = 4
private const val PIN_INPUT_CONFIRMATION_DELAY_MS = 200L

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun PreviewPinEntryCommonScreen() {
    FinanceManagerTheme {
        PinEntryCommonScreen(
            titleId = R.string.input_password,
            errorMessage = "Неверный PIN. Осталось попыток: 3",
            showBiometricButton = true,
            onPinEntered = {}
        )
    }
}

/**
 * Общий экран ввода PIN: заголовок, точки, сообщение и клавиатура.
 *
 * Биометрию экран не запускает сам — лишь сообщает о нажатии кнопки ([onFingerprintClick]):
 * запрос с шифром или без решает вызывающий.
 *
 * @param enabled `false` — ввод запрещён: пауза после ошибок или идёт проверка.
 * @param footer Под клавиатурой — например, «Забыл PIN?».
 */
@Composable
fun PinEntryCommonScreen(
    titleId: Int,
    pinSize: Int = DEFAULT_PIN_SIZE,
    errorMessage: String = "",
    showBiometricButton: Boolean = false,
    enabled: Boolean = true,
    onPinEntered: (String) -> Unit,
    onBackspaceClick: () -> Unit = {},
    onFingerprintClick: () -> Unit = {},
    footer: @Composable ColumnScope.() -> Unit = {}
) {
    val inputPin = remember { mutableStateListOf<Int>() }

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
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
        }

        Keyboard(
            showBiometricButton = showBiometricButton,
            enabled = enabled,
            onNumberClick = { number ->
                if (inputPin.size < pinSize) inputPin.add(number.toInt())
            },
            onBackspaceClick = {
                if (inputPin.isNotEmpty()) {
                    inputPin.removeAt(inputPin.lastIndex)
                    onBackspaceClick()
                }
            },
            onFingerprintClick = onFingerprintClick
        )

        footer()
    }
}
