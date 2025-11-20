package soft.divan.financemanager.feature.transaction.transaction_impl.precenter.model

import java.math.BigDecimal
import java.time.LocalDateTime

// --- MOCK DATA ---

val mockCategories = listOf(
    UiCategory(
        id = 1,
        name = "Еда",
        emoji = "🍔",
        isIncome = false
    ),
    UiCategory(
        id = 2,
        name = "Зарплата",
        emoji = "💰",
        isIncome = true
    ),
    UiCategory(
        id = 3,
        name = "Транспорт",
        emoji = "🚌",
        isIncome = false
    ),
    UiCategory(
        id = 4,
        name = "Подарки",
        emoji = "🎁",
        isIncome = false
    ),
    UiCategory(
        id = 5,
        name = "Инвестиции",
        emoji = "📈",
        isIncome = true
    )
)

val mockTransaction = UiTransaction(
    id = 1001,
    accountId = 1,
    category = mockCategories[0], // "Еда"
    amount = BigDecimal("450.75"),
    amountFormatted = "-450,75 ₽",
    transactionDate = LocalDateTime.now().minusDays(1),
    comment = "Обед в кафе с коллегами",
    createdAt = LocalDateTime.now().minusDays(1).minusHours(2),
    updatedAt = LocalDateTime.now()
)

val mockTransactionUiStateSuccess = TransactionUiState.Success(
    transaction = mockTransaction,
    categories = mockCategories,
    accountName = "Основной счёт"
)

val mockTransactionUiStateLoading = TransactionUiState.Loading

val mockTransactionUiStateError = TransactionUiState.Error(
    message = "Ошибка загрузки данных. Проверьте подключение к интернету."
)