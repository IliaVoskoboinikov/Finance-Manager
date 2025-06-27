package soft.divan.financemanager.presenter.ui.model


sealed interface ExpensesUiState {
    data object Loading : ExpensesUiState
    data class Success(
        val transactions: List<UiTransaction>,
        val sumTransaction: String
    ) : ExpensesUiState

    data class Error(val message: String) : ExpensesUiState
}

