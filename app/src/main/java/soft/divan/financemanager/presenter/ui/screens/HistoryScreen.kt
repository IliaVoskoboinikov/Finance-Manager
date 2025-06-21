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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import soft.divan.financemanager.R
import soft.divan.financemanager.presenter.ui.icons.Arrow
import soft.divan.financemanager.presenter.ui.model.HistoryUiItemList
import soft.divan.financemanager.presenter.ui.model.HistoryUiState
import soft.divan.financemanager.presenter.ui.theme.FinanceManagerTheme
import soft.divan.financemanager.presenter.ui.viewmodel.HistoryViewModel
import soft.divan.financemanager.presenter.uiKit.ContentTextListItem
import soft.divan.financemanager.presenter.uiKit.EmojiCircle
import soft.divan.financemanager.presenter.uiKit.ErrorSnackbar
import soft.divan.financemanager.presenter.uiKit.FMDatePickerDialog
import soft.divan.financemanager.presenter.uiKit.FMDriver
import soft.divan.financemanager.presenter.uiKit.ListItem
import soft.divan.financemanager.presenter.uiKit.LoadingProgressBar
import soft.divan.financemanager.presenter.uiKit.SubContentTextListItem
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter


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
            onEndDateClick = { showEndDatePicker.value = true }
        )
    }
}


object HistoryScreen {
    const val route = "history"
}

@Composable
fun HistoryScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val today = remember { LocalDate.now() }
    val firstDayOfMonth = remember { today.withDayOfMonth(1) }

    var startDate by remember { mutableStateOf(firstDayOfMonth) }
    var endDate by remember { mutableStateOf(today) }

    val showStartDatePicker = remember { mutableStateOf(false) }
    val showEndDatePicker = remember { mutableStateOf(false) }

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
                // viewModel.loadHistory(startDate, endDate)
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
                // viewModel.loadHistory(startDate, endDate)
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

    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy") }

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
                val sortedItems = uiState.items.sortedByDescending { it.getDateTime() }
                Column(modifier = Modifier.padding(innerPadding)) {
                    ListItem(
                        modifier = Modifier.height(56.dp).clickable(onClick = onStartDateClick),
                        content = { ContentTextListItem(stringResource(R.string.start)) },
                        trail = { ContentTextListItem(startDate.format(dateFormatter)) },
                        containerColor = colorScheme.secondaryContainer
                    )
                    FMDriver()
                    ListItem(
                        modifier = Modifier.height(56.dp).clickable(onClick = onEndDateClick),
                        content = { ContentTextListItem(stringResource(R.string.end)) },
                        trail = { ContentTextListItem(endDate.format(dateFormatter)) },
                        containerColor = colorScheme.secondaryContainer
                    )
                    FMDriver()
                    ListItem(
                        modifier = Modifier.height(56.dp),
                        content = { ContentTextListItem(stringResource(R.string.all)) },
                        trail = { val sum = sortedItems.sumOf { (it as? HistoryUiItemList.Item)?.price?.filter { c -> c.isDigit() }?.toIntOrNull() ?: 0 }
                            ContentTextListItem("$sum ₽") },
                        containerColor = colorScheme.secondaryContainer
                    )
                    FMDriver()
                    LazyColumn {
                        items(sortedItems) { item ->
                            RenderHistoryListItem(item)
                            FMDriver()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RenderHistoryListItem(item: HistoryUiItemList) {
    when (item) {
        is HistoryUiItemList.Item -> {
            ListItem(
                modifier = Modifier.height(70.dp),
                lead = {
                    EmojiCircle(emoji = item.emoji)

                },
                content = {
                    Column {
                        ContentTextListItem(item.content)
                        if (item.subContent.isNotEmpty()) {
                            SubContentTextListItem(item.subContent)
                        }
                    }
                },
                trail = {
                    Column {
                        ContentTextListItem(item.price)
                        ContentTextListItem(item.time)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(
                        imageVector = Icons.Filled.Arrow,
                        contentDescription = "arrow",
                        tint = colorScheme.onSurfaceVariant
                    )
                },
            )
        }
    }
}

fun provideMockHistoryUiState(): HistoryUiState.Success {
    return HistoryUiState.Success(
        listOf(
            HistoryUiItemList.Item(
                emoji = "pk",
                content = "Ремонт квартиры",
                subContent = "Фурнитура для дверей",
                price = "58 000 ₽",
                time = "22:01",
                onClick = {}
            ),
            HistoryUiItemList.Item(
                emoji = "🐶",
                content = "На собачку",
                price = "100 000 ₽",
                subContent = "",
                time = "22:01",
                onClick = {}
            ),
            HistoryUiItemList.Item(
                emoji = "🐶",
                content = "На собачку",
                subContent = "",
                price = "100 000 ₽",
                time = "22:01",
                onClick = {}
            ),
            HistoryUiItemList.Item(
                emoji = "🐶",
                content = "На собачку",
                subContent = "",
                price = "100 000 ₽",
                time = "22:01",
                onClick = {}
            ),
            HistoryUiItemList.Item(
                emoji = "🐶",
                content = "На собачку",
                subContent = "",
                price = "100 000 ₽",
                time = "22:01",
                onClick = {}
            ),
        )
    )
}
fun HistoryUiItemList.getDateTime(): LocalDateTime {
    return when (this) {
        is HistoryUiItemList.Item -> {
            // Пример парсинга времени "22:01", используем текущую дату
            try {
                val formatter = DateTimeFormatter.ofPattern("HH:mm")
                val time = LocalTime.parse(this.time, formatter)
                LocalDateTime.of(LocalDate.now(), time)
            } catch (e: Exception) {
                LocalDateTime.MIN
            }
        }
    }
}
