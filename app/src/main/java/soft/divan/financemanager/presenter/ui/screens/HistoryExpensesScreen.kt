package soft.divan.financemanager.presenter.ui.screens

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import soft.divan.financemanager.R
import soft.divan.financemanager.domain.model.Category
import soft.divan.financemanager.domain.model.Transaction
import soft.divan.financemanager.domain.util.DateHelper
import soft.divan.financemanager.presenter.ui.icons.Arrow
import soft.divan.financemanager.presenter.ui.model.HistoryUiState
import soft.divan.financemanager.presenter.ui.theme.FinanceManagerTheme
import soft.divan.financemanager.presenter.ui.viewmodel.HistoryExpensesViewModel
import soft.divan.financemanager.presenter.uiKit.ContentTextListItem
import soft.divan.financemanager.presenter.uiKit.EmojiCircle
import soft.divan.financemanager.presenter.uiKit.ErrorSnackbar
import soft.divan.financemanager.presenter.uiKit.FMDatePickerDialog
import soft.divan.financemanager.presenter.uiKit.FMDriver
import soft.divan.financemanager.presenter.uiKit.ListItem
import soft.divan.financemanager.presenter.uiKit.LoadingProgressBar
import soft.divan.financemanager.presenter.uiKit.SubContentTextListItem
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime


@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun HistoryExpensesScreenPreview() {

    val today = remember { LocalDate.now() }
    val firstDayOfMonth = remember { today.withDayOfMonth(1) }

    var startDate by remember { mutableStateOf(firstDayOfMonth) }
    var endDate by remember { mutableStateOf(today) }

    val showStartDatePicker = remember { mutableStateOf(false) }
    val showEndDatePicker = remember { mutableStateOf(false) }
    FinanceManagerTheme {
      HistoryContent(
            uiState = provideMockHistoryExpensesUiState(),
            startDate = startDate,
            endDate = endDate,
            onStartDateClick = { showStartDatePicker.value = true },
            onEndDateClick = { showEndDatePicker.value = true }
        )
    }
}


object HistoryExpensesScreen {
    const val route = "history_expenses"
}

@Composable
fun HistoryExpensesScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: HistoryExpensesViewModel = hiltViewModel()
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
        onEndDateClick = { showEndDatePicker.value = true }
    )

    if (showStartDatePicker.value) {
        FMDatePickerDialog(
            initialDate = startDate,
            onDateSelected = {
                startDate = it
                showStartDatePicker.value = false
                viewModel.updateStartDate( startDate)
                viewModel.loadHistory(
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
                viewModel.updateEndDate(  endDate)
                viewModel.loadHistory(
                    startDate = startDate,
                    endDate = endDate
                )
            },
            onDismissRequest = { showEndDatePicker.value = false }
        )
    }
}

@Composable
fun HistoryContent(
    modifier: Modifier = Modifier,
    uiState: HistoryUiState,
    startDate: LocalDate,
    endDate: LocalDate,
    onStartDateClick: () -> Unit,
    onEndDateClick: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        when (uiState) {
            is HistoryUiState.Loading -> {
                LoadingProgressBar()
            }

            is HistoryUiState.Error -> {
                ErrorSnackbar(
                    snackbarHostState = snackbarHostState,
                    message = uiState.message,
                )
            }

            is HistoryUiState.Success -> {
                val sortedItems = uiState.transactions.sortedByDescending { it.createdAt }
                Column(modifier = Modifier.padding(innerPadding)) {
                    ListItem(
                        modifier = Modifier
                            .height(56.dp)
                            .clickable(onClick = onStartDateClick),
                        content = { ContentTextListItem(stringResource(R.string.start)) },
                        trail = { ContentTextListItem(DateHelper.formatDateForDisplay(startDate)) },
                        containerColor = colorScheme.secondaryContainer
                    )
                    FMDriver()
                    ListItem(
                        modifier = Modifier
                            .height(56.dp)
                            .clickable(onClick = onEndDateClick),
                        content = { ContentTextListItem(stringResource(R.string.end)) },
                        trail = { ContentTextListItem(DateHelper.formatDateForDisplay(endDate)) },
                        containerColor = colorScheme.secondaryContainer
                    )
                    FMDriver()
                    ListItem(
                        modifier = Modifier.height(56.dp),
                        content = { ContentTextListItem(stringResource(R.string.all)) },
                        trail = { ContentTextListItem("${uiState.sumTransaction} ₽") },
                        containerColor = colorScheme.secondaryContainer
                    )
                    FMDriver()
                    LazyColumn {
                        items(sortedItems) { item ->
                            ListItem(
                                modifier = Modifier.height(70.dp),
                                lead = {
                                    EmojiCircle(emoji = item.category.emoji)

                                },
                                content = {
                                    Column {
                                        ContentTextListItem(item.category.name)
                                        if (!item.comment.isNullOrEmpty()) {
                                            SubContentTextListItem(item.comment)
                                        }
                                    }
                                },
                                trail = {
                                    Column (horizontalAlignment = Alignment.End ){
                                        ContentTextListItem(text = item.amount.toPlainString() + " ₽")
                                        ContentTextListItem(DateHelper.formatDateTimeForDisplay(item.transactionDate))
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
                    }
                }
            }
        }
    }
}


fun provideMockHistoryExpensesUiState(): HistoryUiState.Success {
    val testCategories = listOf(
        Category(1, "Зарплата", "💰", isIncome = true),
        Category(2, "Продукты", "🛒", isIncome = false),
        Category(3, "Транспорт", "🚌", isIncome = false),
        Category(4, "Развлечения", "🎮", isIncome = false),
        Category(5, "Кафе", "☕", isIncome = false),
        Category(6, "Медицинa", "💊", isIncome = false),
        Category(7, "Подарки", "🎁", isIncome = false),
        Category(8, "Образование", "📚", isIncome = false),
        Category(9, "Аренда", "🏠", isIncome = false),
        Category(10, "Проценты", "📈", isIncome = true),
    )
    val now = LocalDateTime.now()
    val testTransactions = listOf(
        Transaction(1, 1, testCategories[0], BigDecimal("120000.00"), now.minusDays(10), "Аванс", now.minusDays(10), now.minusDays(10)),
        Transaction(2, 1, testCategories[1], BigDecimal("3500.50"), now.minusDays(9), "Покупка в Перекрестке", now.minusDays(9), now.minusDays(9)),
        Transaction(3, 1, testCategories[2], BigDecimal("120.00"), now.minusDays(8), "Метро", now.minusDays(8), now.minusDays(8)),
        Transaction(4, 1, testCategories[3], BigDecimal("799.99"), now.minusDays(7), "Steam покупка", now.minusDays(7), now.minusDays(7)),
        Transaction(5, 1, testCategories[4], BigDecimal("450.00"), now.minusDays(6), "Кофе с другом", now.minusDays(6), now.minusDays(6)),
        Transaction(6, 1, testCategories[5], BigDecimal("2500.00"), now.minusDays(5), "Аптека", now.minusDays(5), now.minusDays(5)),
        Transaction(7, 1, testCategories[6], BigDecimal("3000.00"), now.minusDays(4), "Подарок маме", now.minusDays(4), now.minusDays(4)),
        Transaction(8, 1, testCategories[7], BigDecimal("15000.00"), now.minusDays(3), "Курс Android", now.minusDays(3), now.minusDays(3)),
        Transaction(9, 1, testCategories[8], BigDecimal("40000.00"), now.minusDays(2), "Квартира", now.minusDays(2), now.minusDays(2)),
        Transaction(10, 1, testCategories[9], BigDecimal("1200.00"), now.minusDays(1), "Доход по вкладу", now.minusDays(1), now.minusDays(1)),
    )
    return HistoryUiState.Success(
       transactions = testTransactions ,
        sumTransaction = 5000000.toString()
    )
}
