package soft.divan.financemanager.feature.analysis.analysis_impl.precenter.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.yml.charts.common.components.Legends
import co.yml.charts.common.model.LegendLabel
import co.yml.charts.common.model.LegendsConfig
import co.yml.charts.ui.piechart.charts.PieChart
import co.yml.charts.ui.piechart.models.PieChartConfig
import co.yml.charts.ui.piechart.models.PieChartData
import soft.divan.financemanager.core.domain.util.DateHelper
import soft.divan.financemanager.feature.analysis.analysis_impl.R
import soft.divan.financemanager.feature.analysis.analysis_impl.precenter.model.AnalysisUiState
import soft.divan.financemanager.feature.analysis.analysis_impl.precenter.model.mockTransactionUiStateSuccess
import soft.divan.financemanager.feature.analysis.analysis_impl.precenter.viewModel.AnalysisViewModel
import soft.divan.financemanager.uikit.components.ContentTextListItem
import soft.divan.financemanager.uikit.components.ErrorContent
import soft.divan.financemanager.uikit.components.FMDatePickerDialog
import soft.divan.financemanager.uikit.components.FMDriver
import soft.divan.financemanager.uikit.components.ListItem
import soft.divan.financemanager.uikit.components.LoadingProgressBar
import soft.divan.financemanager.uikit.components.TopBar
import soft.divan.financemanager.uikit.icons.ArrowBack
import soft.divan.financemanager.uikit.model.TopBarModel
import soft.divan.financemanager.uikit.theme.FinanceManagerTheme
import java.time.LocalDate


@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun AnalysisScreenPreview() {
    val today = remember { LocalDate.now() }
    val firstDayOfMonth = remember { today.withDayOfMonth(1) }

    val startDate = remember { mutableStateOf(firstDayOfMonth) }
    val endDate = remember { mutableStateOf(today) }
    FinanceManagerTheme {
        AnalysisContent(
            uiState = mockTransactionUiStateSuccess,
            onNavigateBack = { },
            snackbarHostState = remember { SnackbarHostState() },
            onUpdateStartDate = {},
            onUpdateEndDate = {},
            onLoadHistory = { date: LocalDate, date1: LocalDate -> },
            startDate = startDate,
            endDate = endDate
        )
    }
}

@Composable
fun AnalysisScreen(
    modifier: Modifier = Modifier,
    isIncome: Boolean = false,
    onNavigateBack: () -> Unit,
    viewModel: AnalysisViewModel = hiltViewModel(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val today = remember { LocalDate.now() }
    val firstDayOfMonth = remember { today.withDayOfMonth(1) }

    val startDate = remember { mutableStateOf(firstDayOfMonth) }
    val endDate = remember { mutableStateOf(today) }

    LaunchedEffect(isIncome) {
        viewModel.setIsIncome(isIncome)
        viewModel.load(
            startDate = startDate.value,
            endDate = endDate.value
        )
    }

    AnalysisContent(
        modifier = modifier,
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        snackbarHostState = snackbarHostState,
        onUpdateStartDate = viewModel::updateStartDate,
        onUpdateEndDate = viewModel::updateEndDate,
        onLoadHistory = viewModel::load,
        startDate = startDate,
        endDate = endDate
    )
}

@Composable
fun AnalysisContent(
    modifier: Modifier = Modifier,
    uiState: AnalysisUiState,
    onNavigateBack: () -> Unit,
    onUpdateStartDate: (LocalDate) -> Unit,
    onUpdateEndDate: (LocalDate) -> Unit,
    onLoadHistory: (startDate: LocalDate, endDate: LocalDate) -> Unit,
    snackbarHostState: SnackbarHostState,
    startDate: MutableState<LocalDate>,
    endDate: MutableState<LocalDate>
) {
    Scaffold(
        topBar = {
            TopBar(
                topBar = TopBarModel(
                    title = R.string.analysis,
                    navigationIcon = Icons.Filled.ArrowBack,
                    navigationIconClick = onNavigateBack,
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(modifier = modifier.padding(paddingValues)) {
            when (uiState) {
                is AnalysisUiState.Loading -> LoadingProgressBar()
                is AnalysisUiState.Error -> {
//todo
                    ErrorContent({})
                }

                is AnalysisUiState.Success ->
                    AnalysisForm(
                        uiState = uiState,
                        onUpdateStartDate = onUpdateStartDate,
                        onUpdateEndDate = onUpdateEndDate,
                        onLoadHistory = onLoadHistory,
                        startDate = startDate,
                        endDate = endDate,
                    )
            }
        }
    }
}

@Composable
fun AnalysisForm(
    uiState: AnalysisUiState.Success,
    onUpdateStartDate: (LocalDate) -> Unit,
    onUpdateEndDate: (LocalDate) -> Unit,
    onLoadHistory: (startDate: LocalDate, endDate: LocalDate) -> Unit,
    startDate: MutableState<LocalDate>,
    endDate: MutableState<LocalDate>,
) {

    val showStartDatePicker = remember { mutableStateOf(false) }
    val showEndDatePicker = remember { mutableStateOf(false) }

    if (showStartDatePicker.value) {
        FMDatePickerDialog(
            initialDate = startDate.value,
            onDateSelected = {
                startDate.value = it
                showStartDatePicker.value = false
                onUpdateStartDate(startDate.value)
                onLoadHistory(startDate.value, endDate.value)
            },
            onDismissRequest = { showStartDatePicker.value = false }
        )
    }

    if (showEndDatePicker.value) {
        FMDatePickerDialog(
            initialDate = endDate.value,
            onDateSelected = {
                endDate.value = it
                showEndDatePicker.value = false
                onUpdateEndDate(endDate.value)
                onLoadHistory(startDate.value, endDate.value)
            },
            onDismissRequest = { showEndDatePicker.value = false }
        )
    }

    Column() {
        ListItem(
            modifier = Modifier
                .height(56.dp)
                .clickable(onClick = { showStartDatePicker.value = true }),
            content = { ContentTextListItem(stringResource(R.string.period_start)) },
            trail = { ContentTextListItem(DateHelper.formatDateForDisplay(startDate.value)) },
            containerColor = colorScheme.secondaryContainer
        )
        FMDriver()
        ListItem(
            modifier = Modifier
                .height(56.dp)
                .clickable(onClick = { showEndDatePicker.value = true }),
            content = { ContentTextListItem(stringResource(R.string.period_end)) },
            trail = { ContentTextListItem(DateHelper.formatDateForDisplay(endDate.value)) },
            containerColor = colorScheme.secondaryContainer
        )
        FMDriver()
        ListItem(
            modifier = Modifier.height(56.dp),
            content = { ContentTextListItem(stringResource(R.string.summ)) },
            trail = { ContentTextListItem(uiState.sumTransaction) },
            containerColor = colorScheme.secondaryContainer
        )
        FMDriver()

        Diagram(uiState.categoryPieSlice)

    }

}


@Composable
fun Diagram(categoryPieSlice: PieChartData) {

    val donutChartConfig =
        PieChartConfig(
            showSliceLabels = false,
            backgroundColor = MaterialTheme.colorScheme.background,
            isClickOnSliceEnabled = true,
            isAnimationEnable = true,
        )

    var selectedSliceLabel by rememberSaveable { mutableStateOf<String?>(null) }

    Column {
        Legends(
            modifier = Modifier.heightIn(max = 136.dp),
            legendsConfig = getLegendsConfigFromPieChartData(
                categoryPieSlice,
                2
            )
        )
        FMDriver()
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {

            PieChart(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .wrapContentWidth()
                    .padding(8.dp),
                pieChartData = categoryPieSlice,
                pieChartConfig = donutChartConfig,
                onSliceClick = { slice ->

                    val newLabel = "${slice.label} ${slice.value.pretty()}%"

                    selectedSliceLabel =
                        if (selectedSliceLabel == newLabel) null else newLabel

                }
            )

            selectedSliceLabel?.let { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 12.sp,
                )
            }
        }
    }
}

private fun getLegendsConfigFromPieChartData(
    categoryPieSlice: PieChartData,
    gridSize: Int
): LegendsConfig {
    val legendsList = mutableListOf<LegendLabel>()
    categoryPieSlice.slices.forEach { slice ->
        legendsList.add(LegendLabel(slice.color, slice.label + " " + slice.value.pretty() + "%"))
    }
    return LegendsConfig(
        legendLabelList = legendsList,
        gridColumnCount = gridSize,
        legendsArrangement = Arrangement.Start,
        textStyle = TextStyle()
    )
}

fun Float.pretty(): String =
    if ((this % 1).toDouble() == 0.0) this.toInt().toString() else this.toString()
