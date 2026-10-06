package soft.divan.financemanager.feature.security.impl.presenter.screen

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import soft.divan.financemanager.feature.security.impl.R
import soft.divan.financemanager.feature.security.impl.presenter.model.KeyLostUiState
import soft.divan.financemanager.feature.security.impl.presenter.viewmodel.KeyLostViewModel
import soft.divan.financemanager.uikit.theme.FinanceManagerTheme

@Preview(showBackground = true)
@Composable
fun PreviewKeyLostScreen() {
    FinanceManagerTheme {
        KeyLostContent(uiState = KeyLostUiState(), onRetry = {}, onWipe = {})
    }
}

@Preview(name = "Key lost — dark, busy", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewKeyLostScreenBusy() {
    FinanceManagerTheme(darkTheme = true) {
        KeyLostContent(uiState = KeyLostUiState(inProgress = true), onRetry = {}, onWipe = {})
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewKeyLostWipeDialog() {
    FinanceManagerTheme {
        KeyLostWipeDialog(isGuest = false, onConfirm = {}, onDismiss = {})
    }
}

/**
 * Данные не открыть: ключ Keystore утерян или инвалидирован, набор ключей испорчен.
 *
 * Показывается хостом вне навигации; уходит сам, когда данные откроются.
 */
@Composable
fun KeyLostScreen(viewModel: KeyLostViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    KeyLostContent(uiState = uiState, onRetry = viewModel::onRetry, onWipe = viewModel::onWipeClicked)

    if (uiState.showConfirm) {
        KeyLostWipeDialog(
            isGuest = uiState.isGuest,
            onConfirm = viewModel::onWipeConfirmed,
            onDismiss = viewModel::onWipeDismissed
        )
    }
}

/** Экран восстановления без DI. */
@Composable
fun KeyLostContent(
    uiState: KeyLostUiState,
    onRetry: () -> Unit,
    onWipe: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(56.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.key_lost_title),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.key_lost_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(32.dp))
        if (uiState.inProgress) {
            CircularProgressIndicator()
        } else {
            Button(onClick = onRetry, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.key_lost_retry))
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onWipe, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.key_lost_wipe))
            }
        }
    }
}

/** Предупреждение перед стиранием недоступных данных. */
@Composable
fun KeyLostWipeDialog(
    isGuest: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.key_lost_wipe)) },
        text = {
            Text(
                stringResource(
                    if (isGuest) R.string.key_lost_wipe_message_guest else R.string.key_lost_wipe_message
                )
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(R.string.wipe_data_action),
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel_action)) }
        }
    )
}
