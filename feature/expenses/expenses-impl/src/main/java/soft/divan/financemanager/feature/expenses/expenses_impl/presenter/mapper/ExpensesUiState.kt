package soft.divan.financemanager.feature.expenses.expenses_impl.presenter.mapper

import soft.divan.financemanager.core.shared_history_transaction_category.presenter.model.UiTransaction

sealed interface ExpensesUiState {
    data object Loading : ExpensesUiState
    data class Success(
        val transactions: List<UiTransaction>,
        val sumTransaction: String
    ) : ExpensesUiState

    data class Error(val message: String) : ExpensesUiState
}