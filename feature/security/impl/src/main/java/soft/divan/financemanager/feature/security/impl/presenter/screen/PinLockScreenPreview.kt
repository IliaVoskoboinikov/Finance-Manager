package soft.divan.financemanager.feature.security.impl.presenter.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import soft.divan.financemanager.feature.security.impl.R
import soft.divan.financemanager.feature.security.impl.presenter.components.Keyboard
import soft.divan.financemanager.feature.security.impl.presenter.components.PinCodeScreenHeader
import soft.divan.financemanager.feature.security.impl.presenter.components.RoundedBoxesRow
import soft.divan.financemanager.uikit.theme.FinanceManagerTheme

/**
 * Превью экрана PIN-замка для галереи `@Preview`.
 *
 * Живёт в отдельном файле (не в `PinLockScreen.kt`) намеренно: реальный экран использует
 * `BiometricPrompt.AuthenticationCallback` из `android.hardware.biometrics`, а этот тип не
 * загружается в headless-рендере Layoutlib. Все top-level функции одного `.kt` компилируются
 * в один класс, и при поиске превью через `getDeclaredMethods()` резолвятся сигнатуры соседей
 * — поэтому `@Preview` рядом с биометрией падает с `NoClassDefFoundError`. Здесь превью собрано
 * из под-компонентов (заголовок + индикатор ввода + клавиатура с кнопкой биометрии) и от
 * `android.hardware.*` не зависит.
 */
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun PreviewPinLockScreen() {
    FinanceManagerTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            PinCodeScreenHeader(text = stringResource(R.string.input_password))
            RoundedBoxesRow(startQuantity = 4, quantity = 2)
            Keyboard(
                showBiometricButton = true,
                onNumberClick = {},
                onBackspaceClick = {}
            )
        }
    }
}
