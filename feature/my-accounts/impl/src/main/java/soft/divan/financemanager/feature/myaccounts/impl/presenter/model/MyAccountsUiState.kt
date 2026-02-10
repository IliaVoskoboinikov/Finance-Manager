package soft.divan.financemanager.feature.myaccounts.impl.presenter.model

import androidx.annotation.StringRes

sealed interface MyAccountsUiState {
    data object Loading : MyAccountsUiState
    data class Success(val accounts: List<MyAccountsUiModel>) : MyAccountsUiState
    data class Error(@field:StringRes val message: Int) : MyAccountsUiState
    data object EmptyData : MyAccountsUiState
}
// Revue me>>
