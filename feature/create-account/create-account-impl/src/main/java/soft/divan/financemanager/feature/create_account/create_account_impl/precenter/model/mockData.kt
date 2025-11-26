package soft.divan.financemanager.feature.create_account.create_account_impl.precenter.model

import soft.divan.financemanager.feature.create_account.create_account_impl.R


val mockCreateAccountUiStateLoading = CreateAccountUiState.Loading

val mockCreateAccountUiStateSuccess = CreateAccountUiState.Success(
    AccountUiModel(
        id = 1,
        name = "счет1",
        balance = "1000",
        currency = "Rub"
    )
)


val mockCreateAccountUiStateError = CreateAccountUiState.Error(
    message = R.string.error_save
)