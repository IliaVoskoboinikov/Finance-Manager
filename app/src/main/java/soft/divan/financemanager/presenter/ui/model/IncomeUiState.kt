package soft.divan.financemanager.presenter.ui.model

sealed interface IncomeUiState {
    data object Loading : IncomeUiState
    data class Success(
        val transactions: List<UiTransaction>,
        val sumTransaction: String
    ) : IncomeUiState
    data class Error(val message: String) : IncomeUiState
}
