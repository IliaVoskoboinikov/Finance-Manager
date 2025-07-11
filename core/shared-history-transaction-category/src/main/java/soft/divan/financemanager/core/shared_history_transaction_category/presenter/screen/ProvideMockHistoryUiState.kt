package soft.divan.financemanager.feature.expenses_income_shared.presenter.screen

import soft.divan.financemanager.core.domain.model.CurrencyCode
import soft.divan.financemanager.core.shared_history_transaction_category.presenter.model.UiCategory
import soft.divan.financemanager.core.shared_history_transaction_category.presenter.model.UiTransaction
import soft.divan.financemanager.feature.expenses_income_shared.presenter.mapper.formatWith
import soft.divan.financemanager.feature.expenses_income_shared.presenter.model.HistoryUiState
import java.math.BigDecimal
import java.time.LocalDateTime


fun provideMockHistoryUiState(): HistoryUiState.Success {
    val testUiCategories = listOf(
        UiCategory(1, "Зарплата", "💰", isIncome = true),
        UiCategory(2, "Продукты", "🛒", isIncome = false),
        UiCategory(3, "Транспорт", "🚌", isIncome = false),
        UiCategory(4, "Развлечения", "🎮", isIncome = false),
        UiCategory(5, "Кафе", "☕", isIncome = false),
        UiCategory(6, "Медицинa", "💊", isIncome = false),
        UiCategory(7, "Подарки", "🎁", isIncome = false),
        UiCategory(8, "Образование", "📚", isIncome = false),
        UiCategory(9, "Аренда", "🏠", isIncome = false),
        UiCategory(10, "Проценты", "📈", isIncome = true),
    )

    val now = LocalDateTime.now()

    val testUiTransactions = listOf(
        UiTransaction(
            1,
            1,
            testUiCategories[0],
            BigDecimal("120000.00"),
            BigDecimal("120000.00").formatWith(CurrencyCode("RUB")),
            now.minusDays(10),
            "Аванс",
            now.minusDays(10),
            now.minusDays(10)
        ),
        UiTransaction(
            2,
            1,
            testUiCategories[1],
            BigDecimal("3500.50"),
            BigDecimal("3500.50").formatWith(CurrencyCode("RUB")),
            now.minusDays(9),
            "Покупка в Перекрестке",
            now.minusDays(9),
            now.minusDays(9)
        ),
        UiTransaction(
            3,
            1,
            testUiCategories[2],
            BigDecimal("120.00"),
            BigDecimal("120.00").formatWith(CurrencyCode("RUB")),
            now.minusDays(8),
            "Метро",
            now.minusDays(8),
            now.minusDays(8)
        ),
        UiTransaction(
            4,
            1,
            testUiCategories[3],
            BigDecimal("799.99"),
            BigDecimal("799.99").formatWith(CurrencyCode("RUB")),
            now.minusDays(7),
            "Steam покупка",
            now.minusDays(7),
            now.minusDays(7)
        ),
        UiTransaction(
            5,
            1,
            testUiCategories[4],
            BigDecimal("450.00"),
            BigDecimal("450.00").formatWith(CurrencyCode("RUB")),
            now.minusDays(6),
            "Кофе с другом",
            now.minusDays(6),
            now.minusDays(6)
        ),
        UiTransaction(
            6,
            1,
            testUiCategories[5],
            BigDecimal("2500.00"),
            BigDecimal("2500.00").formatWith(CurrencyCode("RUB")),
            now.minusDays(5),
            "Аптека",
            now.minusDays(5),
            now.minusDays(5)
        ),
        UiTransaction(
            7,
            1,
            testUiCategories[6],
            BigDecimal("3000.00"),
            BigDecimal("3000.00").formatWith(CurrencyCode("RUB")),
            now.minusDays(4),
            "Подарок маме",
            now.minusDays(4),
            now.minusDays(4)
        ),
        UiTransaction(
            8,
            1,
            testUiCategories[7],
            BigDecimal("15000.00"),
            BigDecimal("15000.00").formatWith(CurrencyCode("RUB")),
            now.minusDays(3),
            "Курс Android",
            now.minusDays(3),
            now.minusDays(3)
        ),
        UiTransaction(
            9,
            1,
            testUiCategories[8],
            BigDecimal("40000.00"),
            BigDecimal("40000.00").formatWith(CurrencyCode("RUB")),
            now.minusDays(2),
            "Квартира",
            now.minusDays(2),
            now.minusDays(2)
        ),
        UiTransaction(
            10,
            1,
            testUiCategories[9],
            BigDecimal("1200.00"),
            BigDecimal("1200.00").formatWith(CurrencyCode("RUB")),
            now.minusDays(1),
            "Доход по вкладу",
            now.minusDays(1),
            now.minusDays(1)
        ),
    )

    return HistoryUiState.Success(
        transactions = testUiTransactions,
        sumTransaction = "5 000 000"
    )
}