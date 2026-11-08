package soft.divan.financemanager.feature.account.impl.precenter.model

import soft.divan.financemanager.feature.account.impl.R
import java.time.Instant

val mockAccountUiStateLoading = AccountUiState.Loading

val mockAccountUiStateSuccess = AccountUiState.Success(
    account = AccountUi(
        id = "1",
        name = "счет1",
        balance = "1000",
        currencyId = "Rub",
        createdAt = Instant.parse("2024-03-07T09:30:00Z"),
        updatedAt = Instant.parse("2024-03-07T09:30:00Z")
    ),
    mode = AccountMode.Create
)

val mockAccountUiStateError = AccountUiState.Error(
    message = R.string.error_save
)
