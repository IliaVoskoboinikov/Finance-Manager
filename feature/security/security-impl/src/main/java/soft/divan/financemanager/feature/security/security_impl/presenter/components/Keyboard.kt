package soft.divan.financemanager.feature.security.security_impl.presenter.components

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import soft.divan.financemanager.feature.security.security_impl.presenter.util.Dimens
import soft.divan.financemanager.uikit.theme.FinanceManagerTheme

@Preview(showBackground = true)
@Composable
fun PreviewKeyboard() {

    FinanceManagerTheme {
        Keyboard(
            showBiometricButton = true,
            onNumberClick = { number -> Log.d("Keyboard", "Clicked: $number") },
            onBackspaceClick = { Log.d("Keyboard", "Backspace") },
            onFingerprintClick = { Log.d("Keyboard", "Fingerprint") }
        )
    }
}

@Composable
fun Keyboard(
    modifier: Modifier = Modifier,
    showBiometricButton: Boolean = false,
    onNumberClick: (String) -> Unit,
    onBackspaceClick: () -> Unit,
    onFingerprintClick: () -> Unit = {}
) {

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
        ).forEach { row ->
            Row() {
                row.forEach { number ->
                    NumberButton(number, onClick = { onNumberClick(number) })
                }
            }
        }

        Row {
            if (showBiometricButton) {
                IconButton(
                    icon = Icons.Default.Fingerprint,
                    onClick = onFingerprintClick
                )
            } else {
                Spacer(
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(
                            vertical = Dimens.verticalKeyboardButtonPadding,
                            horizontal = Dimens.horizontalKeyboardButtonPadding
                        )
                        .size(Dimens.keyBoardButtonSize)
                )
            }

            NumberButton("0", onClick = { onNumberClick("0") })

            IconButton(
                icon = Icons.AutoMirrored.Filled.Backspace,
                onClick = onBackspaceClick
            )
        }
    }
}
