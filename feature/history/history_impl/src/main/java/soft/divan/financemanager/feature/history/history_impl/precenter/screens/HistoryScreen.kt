package soft.divan.financemanager.feature.history.history_impl.precenter.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import soft.divan.financemanager.core.domain.data.DateHelper
import soft.divan.financemanager.feature.history.history_impl.R
import soft.divan.financemanager.feature.history.history_impl.precenter.model.HistoryUiState
import soft.divan.financemanager.feature.history.history_impl.precenter.model.UiTransaction
import soft.divan.financemanager.feature.history.history_impl.precenter.model.mockHistoryUiStateSuccess
import soft.divan.financemanager.feature.history.history_impl.precenter.viewModel.HistoryViewModel
import soft.divan.financemanager.uikit.components.ContentTextListItem
import soft.divan.financemanager.uikit.components.EmojiCircle
import soft.divan.financemanager.uikit.components.ErrorContent
import soft.divan.financemanager.uikit.components.FMDatePickerDialog
import soft.divan.financemanager.uikit.components.FMDriver
import soft.divan.financemanager.uikit.components.ListItem
import soft.divan.financemanager.uikit.components.LoadingProgressBar
import soft.divan.financemanager.uikit.components.SubContentTextListItem
import soft.divan.financemanager.uikit.components.TopBar
import soft.divan.financemanager.uikit.icons.Arrow
import soft.divan.financemanager.uikit.icons.ArrowBack
import soft.divan.financemanager.uikit.icons.TabletWatch
import soft.divan.financemanager.uikit.model.TopBarModel
import soft.divan.financemanager.uikit.theme.FinanceManagerTheme
import java.time.LocalDate


@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun HistoryScreenPreview() {

    val today = remember { LocalDate.now() }
    FinanceManagerTheme {
        HistoryContent(
            uiState = mockHistoryUiStateSuccess,
            startDate = today,
            endDate = today,
            onUpdateStartDate = {},
            onUpdateEndDate = {},
            onNavigateToTransaction = {},
            onNavigateBack = {},
            onNavigateToAnalysis = {},
        )
    }
}

@Composable
fun HistoryScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    onNavigateToTransaction: (Int) -> Unit,
    onNavigateToAnalysis: () -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val startDate by viewModel.startDate.collectAsStateWithLifecycle()
    val endDate by viewModel.endDate.collectAsStateWithLifecycle()

    HistoryContent(
        modifier = modifier,
        uiState = uiState,
        startDate = startDate,
        endDate = endDate,
        onUpdateStartDate = viewModel::updateStartDate,
        onUpdateEndDate = viewModel::updateEndDate,
        onNavigateToTransaction = onNavigateToTransaction,
        onNavigateBack = onNavigateBack,
        onNavigateToAnalysis = onNavigateToAnalysis
    )
}

@Composable
private fun HistoryContent(
    modifier: Modifier = Modifier,
    uiState: HistoryUiState,
    startDate: LocalDate,
    endDate: LocalDate,
    onUpdateStartDate: (LocalDate) -> Unit,
    onUpdateEndDate: (LocalDate) -> Unit,
    onNavigateToTransaction: (Int) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToAnalysis: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        topBar = {
            HistoryTopBar(
                onNavigateBack = onNavigateBack,
                onNavigateToAnalysis = onNavigateToAnalysis
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(modifier = modifier.padding(paddingValues)) {
            PeriodSelector(
                startDate = startDate,
                endDate = endDate,
                onUpdateStartDate = onUpdateStartDate,
                onUpdateEndDate = onUpdateEndDate
            )

            HistoryStatefulContent(uiState = uiState, onNavigateToTransaction = onNavigateToTransaction)
        }
    }
}


@Composable
private fun HistoryTopBar(onNavigateBack: () -> Unit, onNavigateToAnalysis: () -> Unit) {
    TopBar(
        topBar = TopBarModel(
            title = R.string.my_history,
            navigationIcon = Icons.Filled.ArrowBack,
            navigationIconClick = onNavigateBack,
            actionIcon = Icons.Filled.TabletWatch,
            actionIconClick = onNavigateToAnalysis
        )
    )
}

@Composable
private fun PeriodSelector(
    startDate: LocalDate,
    endDate: LocalDate,
    onUpdateStartDate: (LocalDate) -> Unit,
    onUpdateEndDate: (LocalDate) -> Unit
) {
    val showStartPicker = remember { mutableStateOf(false) }
    val showEndPicker = remember { mutableStateOf(false) }

    DatePicker(showStartPicker, startDate, onUpdateStartDate)
    DatePicker(showEndPicker, endDate, onUpdateEndDate)

    Column {
        DateItem(
            label = stringResource(R.string.start),
            value = DateHelper.formatDateForDisplay(startDate),
            onClick = { showStartPicker.value = true }
        )

        FMDriver()

        DateItem(
            label = stringResource(R.string.end),
            value = DateHelper.formatDateForDisplay(endDate),
            onClick = { showEndPicker.value = true }
        )

        FMDriver()
    }
}

@Composable
private fun DatePicker(
    state: MutableState<Boolean>,
    currentDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    if (state.value) {
        FMDatePickerDialog(
            initialDate = currentDate,
            onDateSelected = {
                state.value = false
                onDateSelected(it)
            },
            onDismissRequest = { state.value = false }
        )
    }
}

@Composable
private fun DateItem(
    label: String,
    value: String,
    onClick: () -> Unit
) {
    ListItem(
        modifier = Modifier
            .height(56.dp)
            .clickable(onClick = onClick),
        content = { ContentTextListItem(label) },
        trail = { ContentTextListItem(value) },
        containerColor = colorScheme.secondaryContainer
    )
}

@Composable
private fun HistoryStatefulContent(uiState: HistoryUiState, onNavigateToTransaction: (Int) -> Unit) {
    when (uiState) {
        is HistoryUiState.Loading -> LoadingProgressBar()
        is HistoryUiState.Error -> ErrorContent(onClick = {}) // TODO
        is HistoryUiState.Success -> HistorySuccessContent(
            sumTransaction = uiState.sumTransaction,
            transactions = uiState.transactions,
            onNavigateToTransaction = onNavigateToTransaction
        )
    }
}

@Composable
private fun HistorySuccessContent(
    sumTransaction: String,
    transactions: List<UiTransaction>,
    onNavigateToTransaction: (Int) -> Unit
) {
    Column {
        SummaryItem(sum = sumTransaction)
        FMDriver()
        ListTransaction(transactions = transactions, onNavigateToTransaction = onNavigateToTransaction)
    }
}

@Composable
private fun SummaryItem(sum: String) {
    ListItem(
        modifier = Modifier.height(56.dp),
        content = { ContentTextListItem(stringResource(R.string.all)) },
        trail = { ContentTextListItem(sum) },
        containerColor = colorScheme.secondaryContainer
    )
}

@Composable
private fun ListTransaction(
    transactions: List<UiTransaction>,
    onNavigateToTransaction: (Int) -> Unit,
) {
    LazyColumn {
        items(
            items = transactions,
            key = { it.id }
        ) { item ->
            ItemTransaction(onNavigateToTransaction, item)
        }
    }
}

@Composable
private fun ItemTransaction(
    onNavigateToTransaction: (Int) -> Unit,
    item: UiTransaction
) {
    ListItem(
        modifier = Modifier
            .height(70.dp)
            .clickable {
                onNavigateToTransaction(item.id)
            },
        lead = { EmojiCircle(emoji = item.category.emoji) },
        content = {
            Column {
                ContentTextListItem(item.category.name)
                if (item.comment.isNotEmpty()) SubContentTextListItem(item.comment)
            }
        },
        trail = {
            Column(horizontalAlignment = Alignment.End) {
                ContentTextListItem(text = item.amountFormatted)
                ContentTextListItem(item.transactionDateTime)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Icon(
                imageVector = Icons.Filled.Arrow,
                contentDescription = "arrow",
                tint = colorScheme.onSurfaceVariant
            )
        },
    )
    FMDriver()
}
