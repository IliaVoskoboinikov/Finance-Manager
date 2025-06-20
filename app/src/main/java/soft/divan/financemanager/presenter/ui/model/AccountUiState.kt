package soft.divan.financemanager.presenter.ui.model


sealed class AccountUiState {
    data object Loading : AccountUiState()
    data class Success(val items: List<AccountUiItem>) : AccountUiState()
    data class Error(val message: String) : AccountUiState()
}

sealed class AccountUiItem {

    data class Balance(
        val emoji: String,
        val label: String,
        val amount: String
    ) : AccountUiItem()

    data class Currency(
        val label: String,
        val symbol: String
    ) : AccountUiItem()
}
