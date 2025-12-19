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
import soft.divan.financemanager.core.domain.data.DateHelper
import soft.divan.financemanager.core.domain.extension.pretty
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
    FinanceManagerTheme {
        AnalysisContent(
            uiState = mockTransactionUiStateSuccess,
            onNavigateBack = { },
            snackbarHostState = remember { SnackbarHostState() },
            onUpdateStartDate = {},
            onUpdateEndDate = {},
            startDate = today,
            endDate = today
        )
    }
}

@Composable
fun AnalysisScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    viewModel: AnalysisViewModel = hiltViewModel(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val startDate by viewModel.startDate.collectAsStateWithLifecycle()
    val endDate by viewModel.endDate.collectAsStateWithLifecycle()

    AnalysisContent(
        modifier = modifier,
        uiState = uiState,
        startDate = startDate,
        endDate = endDate,
        onNavigateBack = onNavigateBack,
        snackbarHostState = snackbarHostState,
        onUpdateStartDate = viewModel::updateStartDate,
        onUpdateEndDate = viewModel::updateEndDate,
    )
}

@Composable
private fun AnalysisContent(
    modifier: Modifier = Modifier,
    uiState: AnalysisUiState,
    startDate: LocalDate,
    endDate: LocalDate,
    onNavigateBack: () -> Unit,
    onUpdateStartDate: (LocalDate) -> Unit,
    onUpdateEndDate: (LocalDate) -> Unit,
    snackbarHostState: SnackbarHostState,

    ) {
    Scaffold(
        topBar = { AnalysisTopBar(onNavigateBack = onNavigateBack) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(modifier = modifier.padding(paddingValues)) {
            PeriodSelector(
                startDate = startDate,
                endDate = endDate,
                onUpdateStartDate = onUpdateStartDate,
                onUpdateEndDate = onUpdateEndDate
            )

            AnalysisStatefulContent(uiState = uiState)
        }
    }
}

@Composable
private fun AnalysisTopBar(onNavigateBack: () -> Unit) {
    TopBar(
        topBar = TopBarModel(
            title = R.string.analysis,
            navigationIcon = Icons.Filled.ArrowBack,
            navigationIconClick = onNavigateBack
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
    val isShowStartPicker = remember { mutableStateOf(false) }
    val isShowEndPicker = remember { mutableStateOf(false) }

    DatePicker(isShowStartPicker, startDate, onUpdateStartDate)
    DatePicker(isShowEndPicker, endDate, onUpdateEndDate)

    Column {
        DateItem(
            label = stringResource(R.string.period_start),
            value = DateHelper.formatDateForDisplay(startDate),
            onClick = { isShowStartPicker.value = true }
        )

        FMDriver()

        DateItem(
            label = stringResource(R.string.period_end),
            value = DateHelper.formatDateForDisplay(endDate),
            onClick = { isShowEndPicker.value = true }
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
private fun AnalysisStatefulContent(uiState: AnalysisUiState) {
    when (uiState) {
        is AnalysisUiState.Loading -> LoadingProgressBar()
        is AnalysisUiState.Error -> ErrorContent(onClick = {}) // TODO
        is AnalysisUiState.Success -> AnalysisSuccessContent(uiState = uiState)
    }
}

@Composable
private fun AnalysisSuccessContent(uiState: AnalysisUiState.Success) {
    Column {
        SummaryItem(sum = uiState.sumTransaction)
        FMDriver()
        CategoryDiagram(data = uiState.categoryPieSlice)
    }
}

@Composable
private fun SummaryItem(sum: String) {
    ListItem(
        modifier = Modifier.height(56.dp),
        content = { ContentTextListItem(stringResource(R.string.summ)) },
        trail = { ContentTextListItem(sum) },
        containerColor = colorScheme.secondaryContainer
    )
}


@Composable
private fun CategoryDiagram(data: PieChartData) {
    val config = PieChartConfig(
        showSliceLabels = false,
        backgroundColor = colorScheme.background,
        isClickOnSliceEnabled = true,
        isAnimationEnable = true,
    )

    var selectedSlice by rememberSaveable { mutableStateOf<String?>(null) }

    Column {
        PieChartLegends(data = data)
        FMDriver()
        PieChartWithCenterLabel(
            data = data,
            config = config,
            selectedSliceLabel = selectedSlice,
            onSliceClick = { slice ->
                val label = "${slice.label} ${slice.value.pretty()}%"
                selectedSlice = if (selectedSlice == label) null else label
            }
        )
    }
}

@Composable
private fun PieChartLegends(data: PieChartData) {
    Legends(
        modifier = Modifier.heightIn(max = 136.dp),
        legendsConfig = buildLegendsConfig(data)
    )
}

private fun buildLegendsConfig(data: PieChartData): LegendsConfig {
    val labels = data.slices.map { slice ->
        LegendLabel(
            color = slice.color,
            name = "${slice.label} ${slice.value.pretty()}%",
        )
    }

    return LegendsConfig(
        legendLabelList = labels,
        gridColumnCount = 2,
        legendsArrangement = Arrangement.Start,
        textStyle = TextStyle()
    )
}

@Composable
private fun PieChartWithCenterLabel(
    data: PieChartData,
    config: PieChartConfig,
    selectedSliceLabel: String?,
    onSliceClick: (PieChartData.Slice) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        PieChart(
            modifier = Modifier
                .background(colorScheme.background)
                .wrapContentWidth()
                .padding(8.dp),
            pieChartData = data,
            pieChartConfig = config,
            onSliceClick = onSliceClick
        )

        selectedSliceLabel?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 12.sp
            )
        }
    }
}
