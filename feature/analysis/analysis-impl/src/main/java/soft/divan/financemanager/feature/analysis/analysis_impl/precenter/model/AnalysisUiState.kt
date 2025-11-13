package soft.divan.financemanager.feature.analysis.analysis_impl.precenter.model

sealed interface AnalysisUiState {
    data object Loading : AnalysisUiState
    data class Success(
        val transaction: UiTransaction,
    ) : AnalysisUiState

    data class Error(val message: String) : AnalysisUiState
}