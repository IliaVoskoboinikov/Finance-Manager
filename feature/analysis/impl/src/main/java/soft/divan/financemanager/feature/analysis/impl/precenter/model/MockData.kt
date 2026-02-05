@file:Suppress("MagicNumber")

package soft.divan.financemanager.feature.analysis.impl.precenter.model

import androidx.compose.ui.graphics.Color
import co.yml.charts.common.model.PlotType
import co.yml.charts.ui.piechart.models.PieChartData
import soft.divan.financemanager.feature.analysis.impl.R

val mockTransactionUiStateSuccess = AnalysisUiState.Success(
    sumTransaction = "1000",
    categoryPieSlice = PieChartData(
        slices = listOf(
            PieChartData.Slice(
                label = "Еда",
                value = 100f,
                color = Color(0xFFE57373)
            ),
            PieChartData.Slice(
                label = "Дом",
                value = 200f,
                color = Color(0xFF2196F3)
            )

        ),
        plotType = PlotType.Bar
    )
)

val mockTransactionUiStateLoading = AnalysisUiState.Loading

val mockTransactionUiStateError = AnalysisUiState.Error(
    messageRes = R.string.error_loading
)
