package soft.divan.financemanager.feature.my_accounts.my_accounts_impl.presenter.model

import soft.divan.financemanager.feature.my_accounts.my_accounts_impl.R


val mockAccountsUi = listOf(
    MyAccountsUiModel(
        id = 1,
        name = "основной счет",
        balance = "1000000$",
        currency = "$",
    ), MyAccountsUiModel(
        id = 2,
        name = "основной счет2",
        balance = "1000000$",
        currency = "$",
    )
)
val mockMyAccountsUiStateSuccess = MyAccountsUiState.Success(
    accounts = mockAccountsUi
)

val mockMyAccountsUiStateLoading = MyAccountsUiState.Loading

val mockMyAccountsUiStateError = MyAccountsUiState.Error(
    message = R.string.error_loading
)