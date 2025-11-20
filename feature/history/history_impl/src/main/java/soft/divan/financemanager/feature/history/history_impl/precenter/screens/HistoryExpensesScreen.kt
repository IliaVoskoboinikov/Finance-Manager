package soft.divan.financemanager.feature.history.history_impl.precenter.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import soft.divan.financemanager.feature.history.history_impl.precenter.viewModel.HistoryViewModel
import soft.divan.financemanager.uikit.components.FMDatePickerDialog
import soft.divan.financemanager.uikit.theme.FinanceManagerTheme
import java.time.LocalDate


@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun HistoryScreenPreview() {

    val today = remember { LocalDate.now() }
    val firstDayOfMonth = remember { today.withDayOfMonth(1) }

    var startDate by remember { mutableStateOf(firstDayOfMonth) }
    var endDate by remember { mutableStateOf(today) }

    val showStartDatePicker = remember { mutableStateOf(false) }
    val showEndDatePicker = remember { mutableStateOf(false) }
    FinanceManagerTheme {
        HistoryContent(
            uiState = provideMockHistoryUiState(),
            startDate = startDate,
            endDate = endDate,
            onStartDateClick = { showStartDatePicker.value = true },
            onEndDateClick = { showEndDatePicker.value = true },
            onNavigateToTransaction = {},
            onNavigateBack = {},
            onNavigateToAnalysis = {}
        )
    }
}

@Composable
fun HistoryScreen(
    modifier: Modifier = Modifier,
    isIncome: Boolean,
    onNavigateBack: () -> Unit,
    onNavigateToTransaction: (Int) -> Unit,
    onNavigateToAnalysis: () -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val today = remember { LocalDate.now() }
    val firstDayOfMonth = remember { today.withDayOfMonth(1) }

    var startDate by remember { mutableStateOf(firstDayOfMonth) }
    var endDate by remember { mutableStateOf(today) }

    val showStartDatePicker = remember { mutableStateOf(false) }
    val showEndDatePicker = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadHistory(
            isIncome = isIncome,
            startDate = startDate,
            endDate = endDate
        )
    }


    HistoryContent(
        modifier = modifier,
        uiState = uiState,
        startDate = startDate,
        endDate = endDate,
        onStartDateClick = { showStartDatePicker.value = true },
        onEndDateClick = { showEndDatePicker.value = true },
        onNavigateToTransaction = onNavigateToTransaction,
        onNavigateBack = onNavigateBack,
        onNavigateToAnalysis = onNavigateToAnalysis
    )

    if (showStartDatePicker.value) {
        FMDatePickerDialog(
            initialDate = startDate,
            onDateSelected = {
                startDate = it
                showStartDatePicker.value = false
                viewModel.updateStartDate(startDate)
                viewModel.loadHistory(
                    isIncome = isIncome,
                    startDate = startDate,
                    endDate = endDate
                )
            },
            onDismissRequest = { showStartDatePicker.value = false }
        )
    }

    if (showEndDatePicker.value) {
        FMDatePickerDialog(
            initialDate = endDate,
            onDateSelected = {
                endDate = it
                showEndDatePicker.value = false
                viewModel.updateEndDate(endDate)
                viewModel.loadHistory(
                    isIncome = isIncome,
                    startDate = startDate,
                    endDate = endDate
                )
            },
            onDismissRequest = { showEndDatePicker.value = false }
        )
    }
}

