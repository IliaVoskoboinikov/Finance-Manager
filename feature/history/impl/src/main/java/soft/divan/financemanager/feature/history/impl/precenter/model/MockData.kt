@file:Suppress("MagicNumber")

package soft.divan.financemanager.feature.history.impl.precenter.model

import soft.divan.financemanager.feature.history.impl.R
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

val testUiCategories = listOf(
    UiCategory("Зарплата", "💰"),
    UiCategory("Продукты", "🛒"),
    UiCategory("Транспорт", "🚌"),
    UiCategory("Развлечения", "🎮"),
    UiCategory("Кафе", "☕"),
    UiCategory("Медицина", "💊"),
    UiCategory("Подарки", "🎁"),
    UiCategory("Образование", "📚"),
    UiCategory("Аренда", "🏠"),
    UiCategory("Проценты", "📈")
)

val now = LocalDateTime.now()
val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")

val testUiTransactions = listOf(
    UiTransaction(
        id = "1",
        category = testUiCategories[0],
        amountFormatted = "120000.00₽",
        transactionDateTime = now.minusDays(10).format(dateFormatter),
        comment = "Аванс"
    ),
    UiTransaction(
        id = "2",
        category = testUiCategories[1],
        amountFormatted = "3500.50₽",
        transactionDateTime = now.minusDays(9).format(dateFormatter),
        comment = "Покупка в Перекрестке"
    ),
    UiTransaction(
        id = "3",
        category = testUiCategories[2],
        amountFormatted = "120.00₽",
        transactionDateTime = now.minusDays(8).format(dateFormatter),
        comment = "Метро"
    ),
    UiTransaction(
        id = "4",
        category = testUiCategories[3],
        amountFormatted = "799.99₽",
        transactionDateTime = now.minusDays(7).format(dateFormatter),
        comment = "Steam покупка"
    ),
    UiTransaction(
        id = "5",
        category = testUiCategories[4],
        amountFormatted = "450.00₽",
        transactionDateTime = now.minusDays(6).format(dateFormatter),
        comment = "Кофе с другом"
    ),
    UiTransaction(
        id = "6",
        category = testUiCategories[5],
        amountFormatted = "2500.00₽",
        transactionDateTime = now.minusDays(5).format(dateFormatter),
        comment = "Аптека"
    ),
    UiTransaction(
        id = "7",
        category = testUiCategories[6],
        amountFormatted = "3000.00₽",
        transactionDateTime = now.minusDays(4).format(dateFormatter),
        comment = "Подарок маме"
    ),
    UiTransaction(
        id = "8",
        category = testUiCategories[7],
        amountFormatted = "15000.00₽",
        transactionDateTime = now.minusDays(3).format(dateFormatter),
        comment = "Курс Android"
    ),
    UiTransaction(
        id = "9",
        category = testUiCategories[8],
        amountFormatted = "40000.00₽",
        transactionDateTime = now.minusDays(2).format(dateFormatter),
        comment = "Квартира"
    ),
    UiTransaction(
        id = "10",
        category = testUiCategories[9],
        amountFormatted = "1200.00₽",
        transactionDateTime = now.minusDays(1).format(dateFormatter),
        comment = "Доход по вкладу"
    )
)

val mockHistoryUiStateSuccess = HistoryUiState.Success(
    transactions = testUiTransactions,
    sumTransaction = "50000"
)

val mockHistoryUiStateLoading = HistoryUiState.Loading

val mockTHistoryUiStateError = HistoryUiState.Error(
    message = R.string.error_loading
)
// Revue me>>
