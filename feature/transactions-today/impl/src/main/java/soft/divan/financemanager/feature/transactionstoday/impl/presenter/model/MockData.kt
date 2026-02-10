@file:Suppress("MagicNumber")

package soft.divan.financemanager.feature.transactionstoday.impl.presenter.model

import soft.divan.financemanager.feature.transactions_today.impl.R
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

val testUiCategories = listOf(
    CategoryUi("Зарплата", "💰"),
    CategoryUi("Продукты", "🛒"),
    CategoryUi("Транспорт", "🚌"),
    CategoryUi("Развлечения", "🎮"),
    CategoryUi("Кафе", "☕"),
    CategoryUi("Медицина", "💊"),
    CategoryUi("Подарки", "🎁"),
    CategoryUi("Образование", "📚"),
    CategoryUi("Аренда", "🏠"),
    CategoryUi("Проценты", "📈")
)

val now = LocalDateTime.now()
val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")

val testTransactionUis = listOf(
    TransactionUi(
        id = "1",
        category = testUiCategories[0],
        amountFormatted = "120000.00₽",
        transactionDateTime = now.minusDays(10).format(dateFormatter),
        comment = "Аванс"
    ),
    TransactionUi(
        id = "2",
        category = testUiCategories[1],
        amountFormatted = "3500.50₽",
        transactionDateTime = now.minusDays(9).format(dateFormatter),
        comment = "Покупка в Перекрестке"
    ),
    TransactionUi(
        id = "3",
        category = testUiCategories[2],
        amountFormatted = "120.00₽",
        transactionDateTime = now.minusDays(8).format(dateFormatter),
        comment = "Метро"
    ),
    TransactionUi(
        id = "4",
        category = testUiCategories[3],
        amountFormatted = "799.99₽",
        transactionDateTime = now.minusDays(7).format(dateFormatter),
        comment = "Steam покупка"
    ),
    TransactionUi(
        id = "5",
        category = testUiCategories[4],
        amountFormatted = "450.00₽",
        transactionDateTime = now.minusDays(6).format(dateFormatter),
        comment = "Кофе с другом"
    ),
    TransactionUi(
        id = "6",
        category = testUiCategories[5],
        amountFormatted = "2500.00₽",
        transactionDateTime = now.minusDays(5).format(dateFormatter),
        comment = "Аптека"
    ),
    TransactionUi(
        id = "7",
        category = testUiCategories[6],
        amountFormatted = "3000.00₽",
        transactionDateTime = now.minusDays(4).format(dateFormatter),
        comment = "Подарок маме"
    ),
    TransactionUi(
        id = "8",
        category = testUiCategories[7],
        amountFormatted = "15000.00₽",
        transactionDateTime = now.minusDays(3).format(dateFormatter),
        comment = "Курс Android"
    ),
    TransactionUi(
        id = "9",
        category = testUiCategories[8],
        amountFormatted = "40000.00₽",
        transactionDateTime = now.minusDays(2).format(dateFormatter),
        comment = "Квартира"
    ),
    TransactionUi(
        id = "10",
        category = testUiCategories[9],
        amountFormatted = "1200.00₽",
        transactionDateTime = now.minusDays(1).format(dateFormatter),
        comment = "Доход по вкладу"
    )
)

val mockTransactionsTodayUiStateSuccess = TransactionsTodayUiState.Success(
    transactions = testTransactionUis,
    sumTransaction = "50000"
)

val mockTransactionsTodayUiStateLoading = TransactionsTodayUiState.Loading

val mockTransactionsTodayUiStateError = TransactionsTodayUiState.Error(
    messageRes = R.string.error_loading
)
// Revue me>>
