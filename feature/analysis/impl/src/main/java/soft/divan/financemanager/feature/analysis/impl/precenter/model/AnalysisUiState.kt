package soft.divan.financemanager.feature.analysis.impl.precenter.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import co.yml.charts.ui.piechart.models.PieChartData
import soft.divan.financemanager.core.domain.data.DateHelper

@Immutable
sealed interface AnalysisUiState {
    data object Loading : AnalysisUiState
    data class Success(
        val sumTransaction: String,
        val categoryPieSlice: PieChartData,
        val startDate: String = DateHelper.getCurrentMonthStartDisplayFormat(),
        val endDate: String = DateHelper.getTodayDisplayFormat(),
    ) : AnalysisUiState

    data class Error(@field:StringRes val messageRes: Int) : AnalysisUiState
}