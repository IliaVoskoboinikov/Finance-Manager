package soft.divan.financemanager.feature.analysis.impl.precenter.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import co.yml.charts.ui.piechart.models.PieChartData

@Immutable
sealed interface AnalysisUiState {
    data object Loading : AnalysisUiState
    data class Success(
        val sumTransaction: String,
        val categoryPieSlice: PieChartData
    ) : AnalysisUiState

    data class Error(@field:StringRes val messageRes: Int) : AnalysisUiState
    data object EmptyData : AnalysisUiState
}
