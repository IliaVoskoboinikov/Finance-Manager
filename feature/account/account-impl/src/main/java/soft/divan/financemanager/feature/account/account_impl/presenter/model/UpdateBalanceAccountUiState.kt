package soft.divan.financemanager.feature.account.account_impl.presenter.model

sealed interface UpdateBalanceAccountUiState {
    data object Loading : UpdateBalanceAccountUiState
    data class Success(val u: Unit) : UpdateBalanceAccountUiState
    data class Error(val message: String) : UpdateBalanceAccountUiState
}