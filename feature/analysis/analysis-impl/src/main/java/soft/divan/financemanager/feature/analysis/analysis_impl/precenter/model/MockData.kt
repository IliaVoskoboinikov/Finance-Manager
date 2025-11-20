package soft.divan.financemanager.feature.analysis.analysis_impl.precenter.model


import androidx.compose.ui.graphics.Color
import co.yml.charts.common.model.PlotType
import co.yml.charts.ui.piechart.models.PieChartData

// --- MOCK DATA ---


val mockTransactionUiStateSuccess = AnalysisUiState.Success(
    sumTransaction = "1000",
    categoryPieSlice = PieChartData(
        slices = listOf(
            PieChartData.Slice(
                label = "Еда",
                value = 100f,
                color = Color(0xFFE57373)
            )

        ),
        plotType = PlotType.Bar
    ),

    )

val mockTransactionUiStateLoading = AnalysisUiState.Loading

val mockTransactionUiStateError = AnalysisUiState.Error(
    message = "Ошибка загрузки данных. Проверьте подключение к интернету."
)