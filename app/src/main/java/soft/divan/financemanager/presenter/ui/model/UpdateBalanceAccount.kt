package soft.divan.financemanager.presenter.ui.model

sealed class UpdateBalanceAccountUiState {
    data object Loading : UpdateBalanceAccountUiState()
    data class Success(val u: Unit) : UpdateBalanceAccountUiState()
    data class Error(val message: String) : UpdateBalanceAccountUiState()
}