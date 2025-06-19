package soft.divan.financemanager.presenter.ui.model


sealed class AccountUiState {
    data object Loading : AccountUiState()
    data class Success(val items: List<AccountItem>) : AccountUiState()
    data class Error(val message: String) : AccountUiState()
}

sealed class AccountItem {

    data class Balance(
        val emoji: String,
        val label: String,
        val amount: String
    ) : AccountItem()

    data class Currency(
        val label: String,
        val symbol: String
    ) : AccountItem()
}
