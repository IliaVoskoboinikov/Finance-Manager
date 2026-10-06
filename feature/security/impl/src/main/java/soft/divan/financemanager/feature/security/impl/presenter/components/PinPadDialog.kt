package soft.divan.financemanager.feature.security.impl.presenter.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import soft.divan.financemanager.feature.security.impl.R
import soft.divan.financemanager.feature.security.impl.presenter.model.PinPrompt
import soft.divan.financemanager.feature.security.impl.presenter.model.PinPurpose
import soft.divan.financemanager.feature.security.impl.presenter.screen.PinEntryCommonScreen
import soft.divan.financemanager.uikit.theme.FinanceManagerTheme

@Preview(showBackground = true)
@Composable
fun PreviewPinPadContent() {
    FinanceManagerTheme {
        PinPadContent(prompt = PinPrompt(PinPurpose.DeletePin), onPinEntered = {}, onDismiss = {})
    }
}

@Preview(showBackground = true, fontScale = 1.5f)
@Composable
fun PreviewPinPadContentCreate() {
    FinanceManagerTheme(darkTheme = true) {
        PinPadContent(
            prompt = PinPrompt(PinPurpose.EnablePinLevel, create = true),
            onPinEntered = {},
            onDismiss = {}
        )
    }
}

/**
 * Ввод PIN поверх экрана настроек — полноэкранное окно.
 *
 * Окно наследует `FLAG_SECURE` родителя (`SecureFlagPolicy.Inherit` по умолчанию): диалог —
 * отдельное окно, и без наследования ввод PIN попал бы в скриншот.
 */
@Composable
fun PinPadDialog(
    prompt: PinPrompt,
    onPinEntered: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        PinPadContent(prompt = prompt, onPinEntered = onPinEntered, onDismiss = onDismiss)
    }
}

/**
 * Содержимое ввода PIN. В режиме [PinPrompt.create] PIN вводится дважды: наружу уходит только
 * совпавший, при расхождении ввод начинается заново.
 */
@Composable
fun PinPadContent(
    prompt: PinPrompt,
    onPinEntered: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var firstEntry by rememberSaveable(prompt) { mutableStateOf<String?>(null) }
    var mismatch by rememberSaveable(prompt) { mutableStateOf(false) }
    val repeating = prompt.create && firstEntry != null

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box {
            PinEntryCommonScreen(
                titleId = if (repeating) R.string.repeat_pin else prompt.titleRes,
                errorMessage = if (mismatch) stringResource(R.string.pin_codes_do_not_match) else "",
                onPinEntered = { pin ->
                    val first = firstEntry
                    when {
                        !prompt.create -> onPinEntered(pin)

                        first == null -> {
                            firstEntry = pin
                            mismatch = false
                        }

                        first == pin -> onPinEntered(pin)

                        else -> {
                            firstEntry = null
                            mismatch = true
                        }
                    }
                }
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.cancel_action)
                )
            }
        }
    }
}
