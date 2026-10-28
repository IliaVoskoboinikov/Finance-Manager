package soft.divan.financemanager.uikit.components

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import soft.divan.financemanager.core.uikit.R
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FMDatePickerDialog(
    initialDate: LocalDate,
    onDateSelect: (LocalDate) -> Unit,
    onDismissRequest: () -> Unit
) {
    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.toDatePickerMillis()
    )

    DatePickerDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = {
                pickerState.selectedDateMillis?.let {
                    onDateSelect(datePickerMillisToLocalDate(it))
                    onDismissRequest()
                }
            }) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.cancel))
            }
        },
        colors = DatePickerDefaults.colors()
            .copy(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        DatePicker(state = pickerState, title = null, headline = null, showModeToggle = false)
    }
}
