package soft.divan.financemanager.uikit.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import soft.divan.financemanager.core.uikit.R

/**
 * Диалог подтверждения удаления.
 *
 * Видимостью владеет вызывающий: диалог только сообщает о закрытии через [onDismissRequest]
 * (кнопка «Отмена», тап мимо, «Назад», а также после подтверждения).
 *
 * @param onDismissRequest диалог нужно скрыть.
 * @param onDelete пользователь подтвердил удаление; вызывается сразу после [onDismissRequest].
 */
@Composable
fun DeleteDialog(onDismissRequest: () -> Unit, onDelete: () -> Unit) {
    AlertDialog(
        containerColor = colorScheme.secondaryContainer,
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = {
                onDismissRequest()
                onDelete()
            }) {
                Text(stringResource(R.string.delete), color = colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.cancel), color = colorScheme.onSecondaryContainer)
            }
        },
        title = { Text(stringResource(R.string.delete) + "?") },
        text = { Text(stringResource(R.string.this_action_cannot_be_undone)) }
    )
}
