package soft.divan.financemanager.presenter.ui.model

import soft.divan.financemanager.domain.model.Transaction
import java.math.BigDecimal


sealed class ExpensesUiState {
    data object Loading : ExpensesUiState()
    data class Success(
        val transactions: List<Transaction>,
        val sumTransaction: String
    ) : ExpensesUiState()
    data class Error(val message: String) : ExpensesUiState()
}