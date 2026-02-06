package soft.divan.financemanager.feature.transaction.impl.precenter.model

import soft.divan.financemanager.core.domain.model.CurrencySymbol
import soft.divan.financemanager.feature.transaction.impl.R

// --- MOCK DATA ---

val mockCategories = listOf(
    CategoryUi(
        id = 1,
        name = "Еда",
        emoji = "🍔"
    ),
    CategoryUi(
        id = 2,
        name = "Зарплата",
        emoji = "💰"
    ),
    CategoryUi(
        id = 3,
        name = "Транспорт",
        emoji = "🚌"
    ),
    CategoryUi(
        id = 4,
        name = "Подарки",
        emoji = "🎁"
    ),
    CategoryUi(
        id = 5,
        name = "Инвестиции",
        emoji = "📈"
    )
)

val mockAccounts = listOf(AccountUi("1", "Основной счёт", "1000", CurrencySymbol.RUB.symbol))

val mockTransaction = TransactionUi(
    id = "1001",
    accountId = "1",
    category = mockCategories[0], // "Еда"
    amount = "450.75",
    comment = "Обед в кафе с коллегами",
    createdAt = "dd.MM.yyyy HH:mm ",
    updatedAt = "dd.MM.yyyy HH:mm ",
    currencyCode = CurrencySymbol.RUB.symbol,
    mode = TransactionMode.Create,
    date = "dd.MM.yyyy",
    time = " HH:mm "
)

val mockTransactionUiStateSuccess = TransactionUiState.Success(
    transaction = mockTransaction,
    categories = mockCategories,
    accounts = mockAccounts
)

val mockTransactionUiStateLoading = TransactionUiState.Loading

val mockTransactionUiStateError = TransactionUiState.Error(
    messageRes = R.string.error_load_transaction
)
// Revue me>>
