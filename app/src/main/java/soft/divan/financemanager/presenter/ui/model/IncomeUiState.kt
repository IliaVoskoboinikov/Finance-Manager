package soft.divan.financemanager.presenter.ui.model

import soft.divan.financemanager.domain.model.Transaction

sealed interface IncomeUiState {
    data object Loading : IncomeUiState
    data class Success(
        val transactions: List<Transaction>,
        val sumTransaction: String
    ) : IncomeUiState
    data class Error(val message: String) : IncomeUiState
}
