package soft.divan.financemanager.feature.expanses.presenter.mapper

import soft.divan.financemanager.feature.expenses_income_shared.presenter.model.UiTransaction

sealed interface ExpensesUiState {
    data object Loading : ExpensesUiState
    data class Success(
        val transactions: List<UiTransaction>,
        val sumTransaction: String
    ) : ExpensesUiState

    data class Error(val message: String) : ExpensesUiState
}