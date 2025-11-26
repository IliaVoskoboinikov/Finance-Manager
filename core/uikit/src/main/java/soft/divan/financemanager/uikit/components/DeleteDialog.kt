package soft.divan.financemanager.uikit.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.res.stringResource
import soft.divan.financemanager.core.uikit.R

@Composable
fun DeleteDialog(isShowDeleteDialog: MutableState<Boolean>, onDelete: () -> Unit) {
    AlertDialog(
        containerColor = colorScheme.secondaryContainer,
        onDismissRequest = { isShowDeleteDialog.value = false },
        confirmButton = {
            TextButton(onClick = {
                isShowDeleteDialog.value = false
                onDelete()
            }) {
                Text(stringResource(R.string.delete), color = colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = { isShowDeleteDialog.value = false }) {
                Text(stringResource(R.string.cancel), color = colorScheme.onSecondaryContainer)
            }
        },
        title = { Text(stringResource(R.string.delete) + "?") },
        text = { Text(stringResource(R.string.this_action_cannot_be_undone)) }
    )
}