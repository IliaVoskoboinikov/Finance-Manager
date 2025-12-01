package soft.divan.financemanager.feature.account.account_impl.precenter.model

import soft.divan.financemanager.feature.account.account_impl.R


val mockAccountUiStateLoading = AccountUiState.Loading

val mockAccountUiStateSuccess = AccountUiState.Success(
    account = AccountUiModel(
        id = 1,
        name = "счет1",
        balance = "1000",
        currency = "Rub",
    ),
    mode = AccountMode.Create
)


val mockAccountUiStateError = AccountUiState.Error(
    message = R.string.error_save
)