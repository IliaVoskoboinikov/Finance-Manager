package soft.divan.financemanager.feature.account.impl.precenter.screens

import android.content.res.Configuration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import soft.divan.financemanager.feature.account.impl.R
import soft.divan.financemanager.uikit.theme.FinanceManagerTheme
import soft.divan.financemanager.core.uikit.R as UikitR

/**
 * Диалог подтверждения удаления счёта с адаптивным текстом.
 *
 * Если у счёта есть операции ([hasTransactions] == true), удалить его физически нельзя — он будет
 * заархивирован (скрыт из списков, но операции сохранятся в истории), и диалог предупреждает об
 * этом. Если операций нет — счёт удаляется навсегда.
 *
 * @param hasTransactions есть ли у счёта операции (определяет заголовок, текст и подпись кнопки).
 * @param onConfirm вызывается при подтверждении действия.
 * @param onDismiss вызывается при отмене или закрытии диалога.
 */
@Composable
fun DeleteAccountDialog(
    hasTransactions: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val titleRes =
        if (hasTransactions) R.string.archive_account_title else R.string.delete_account_title
    val messageRes =
        if (hasTransactions) R.string.archive_account_message else R.string.delete_account_message
    val confirmRes = if (hasTransactions) R.string.archive else UikitR.string.delete

    AlertDialog(
        containerColor = colorScheme.secondaryContainer,
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(confirmRes), color = colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(UikitR.string.cancel), color = colorScheme.onSecondaryContainer)
            }
        },
        title = { Text(stringResource(titleRes)) },
        text = { Text(stringResource(messageRes)) }
    )
}

@Preview(showBackground = true, name = "Delete (no transactions)")
@Composable
fun DeleteAccountDialogPreview() {
    FinanceManagerTheme {
        DeleteAccountDialog(hasTransactions = false, onConfirm = {}, onDismiss = {})
    }
}

@Preview(
    showBackground = true,
    name = "Archive (has transactions), dark",
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun ArchiveAccountDialogPreview() {
    FinanceManagerTheme {
        DeleteAccountDialog(hasTransactions = true, onConfirm = {}, onDismiss = {})
    }
}
