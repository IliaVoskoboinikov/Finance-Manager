package soft.divan.financemanager.feature.my_accounts.my_accounts_impl.presenter.model

import androidx.annotation.StringRes

sealed interface MyAccountsUiState {
    data object Loading : MyAccountsUiState
    data class Success(val accounts: List<MyAccountsUiModel>) : MyAccountsUiState
    data class Error(@field:StringRes val message: Int) : MyAccountsUiState
}