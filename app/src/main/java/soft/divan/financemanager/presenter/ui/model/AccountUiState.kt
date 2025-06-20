package soft.divan.financemanager.presenter.ui.model

import soft.divan.financemanager.domain.model.Account


sealed class AccountUiState {
    data object Loading : AccountUiState()
    data class Success(val account: Account) : AccountUiState()
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
