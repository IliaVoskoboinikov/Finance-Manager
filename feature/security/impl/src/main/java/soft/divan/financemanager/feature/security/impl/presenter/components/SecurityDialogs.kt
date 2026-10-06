package soft.divan.financemanager.feature.security.impl.presenter.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import soft.divan.financemanager.core.domain.model.DataProtectionLevel
import soft.divan.financemanager.feature.security.impl.R
import soft.divan.financemanager.feature.security.impl.presenter.model.SecurityDialog
import soft.divan.financemanager.uikit.theme.FinanceManagerTheme

@Preview(showBackground = true)
@Composable
fun PreviewConfirmPinLevelDialogGuest() {
    FinanceManagerTheme {
        SecurityDialogHost(
            dialog = SecurityDialog.ConfirmLevel(DataProtectionLevel.PIN),
            isGuest = true,
            onConfirmLevel = {},
            onAcceptBiometric = {},
            onDismiss = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewConfirmNoneLevelDialog() {
    FinanceManagerTheme(darkTheme = true) {
        SecurityDialogHost(
            dialog = SecurityDialog.ConfirmLevel(DataProtectionLevel.NONE),
            isGuest = false,
            onConfirmLevel = {},
            onAcceptBiometric = {},
            onDismiss = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewUnsentChangesDialog() {
    FinanceManagerTheme {
        SecurityDialogHost(
            dialog = SecurityDialog.UnsentChanges,
            isGuest = false,
            onConfirmLevel = {},
            onAcceptBiometric = {},
            onDismiss = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewOfferBiometricDialog() {
    FinanceManagerTheme {
        SecurityDialogHost(
            dialog = SecurityDialog.OfferBiometric,
            isGuest = false,
            onConfirmLevel = {},
            onAcceptBiometric = {},
            onDismiss = {}
        )
    }
}

/**
 * Диалоги экрана настроек безопасности.
 *
 * Предупреждения о смене уровня говорят о последствиях честно: на уровне PIN нет фоновой
 * синхронизации, а забытый PIN означает стирание — для гостя безвозвратное.
 */
@Composable
fun SecurityDialogHost(
    dialog: SecurityDialog,
    isGuest: Boolean,
    onConfirmLevel: (DataProtectionLevel) -> Unit,
    onAcceptBiometric: () -> Unit,
    onDismiss: () -> Unit
) {
    when (dialog) {
        is SecurityDialog.ConfirmLevel -> ConfirmLevelDialog(
            target = dialog.target,
            isGuest = isGuest,
            onConfirm = { onConfirmLevel(dialog.target) },
            onDismiss = onDismiss
        )

        SecurityDialog.UnsentChanges -> AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(R.string.unsent_changes_title)) },
            text = { Text(stringResource(R.string.unsent_changes_message)) },
            confirmButton = {
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.ok_action)) }
            }
        )

        SecurityDialog.OfferBiometric -> AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(R.string.offer_biometric_title)) },
            text = { Text(stringResource(R.string.offer_biometric_message)) },
            confirmButton = {
                TextButton(onClick = onAcceptBiometric) { Text(stringResource(R.string.enable_action)) }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.not_now_action)) }
            }
        )
    }
}

@Composable
private fun ConfirmLevelDialog(
    target: DataProtectionLevel,
    isGuest: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val message = when (target) {
        DataProtectionLevel.NONE -> stringResource(R.string.confirm_level_none_message)

        DataProtectionLevel.DEVICE -> stringResource(R.string.confirm_level_device_message)

        DataProtectionLevel.PIN -> buildString {
            append(stringResource(R.string.confirm_level_pin_message))
            if (isGuest) {
                append("\n\n")
                append(stringResource(R.string.confirm_level_pin_guest_warning))
            }
        }
    }
    val title = when (target) {
        DataProtectionLevel.NONE -> R.string.confirm_level_none_title
        DataProtectionLevel.DEVICE -> R.string.confirm_level_device_title
        DataProtectionLevel.PIN -> R.string.confirm_level_pin_title
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(title)) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(R.string.continue_action),
                    color = if (target == DataProtectionLevel.NONE) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel_action)) }
        }
    )
}
