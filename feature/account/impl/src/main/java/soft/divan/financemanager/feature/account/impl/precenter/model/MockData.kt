package soft.divan.financemanager.feature.account.impl.precenter.model

import soft.divan.financemanager.feature.account.impl.R

val mockAccountUiStateLoading = AccountUiState.Loading

val mockAccountUiStateSuccess = AccountUiState.Success(
    account = AccountUi(
        id = "1",
        name = "счет1",
        balance = "1000",
        currency = "Rub",
        createdAt = "dd.MM.yyyy HH:mm ",
        updatedAt = "dd.MM.yyyy HH:mm "
    ),
    mode = AccountMode.Create
)

val mockAccountUiStateError = AccountUiState.Error(
    message = R.string.error_save
)
// Revue me>>
