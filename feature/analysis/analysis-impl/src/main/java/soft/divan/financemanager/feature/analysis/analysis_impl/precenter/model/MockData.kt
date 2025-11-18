package soft.divan.financemanager.feature.analysis.analysis_impl.precenter.model


import androidx.compose.ui.graphics.Color
import co.yml.charts.common.model.PlotType
import co.yml.charts.ui.piechart.models.PieChartData
import soft.divan.financemanager.core.shared_history_transaction_category.presenter.model.UiCategory
import soft.divan.financemanager.core.shared_history_transaction_category.presenter.model.UiTransaction
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

val mockTransactions = listOf(
    UiTransaction(
        id = 1001,
        accountId = 1,
        category = mockCategories[0], // "Еда"
        amount = BigDecimal("450.75"),
        amountFormatted = "-450,75 ₽",
        transactionDate = LocalDateTime.now().minusDays(1),
        comment = "Обед в кафе с коллегами",
        createdAt = LocalDateTime.now().minusDays(1).minusHours(2),
        updatedAt = LocalDateTime.now()
    ), UiTransaction(
        id = 1002,
        accountId = 1,
        category = mockCategories[0], // "Еда"
        amount = BigDecimal("450.75"),
        amountFormatted = "-450,75 ₽",
        transactionDate = LocalDateTime.now().minusDays(1),
        comment = "Обед в кафе с коллегами",
        createdAt = LocalDateTime.now().minusDays(1).minusHours(2),
        updatedAt = LocalDateTime.now()
    )
)

val mockTransactionUiStateSuccess = AnalysisUiState.Success(
    transactions = mockTransactions,
    sumTransaction = "1000",
    categoryPieSlice = PieChartData(
        slices = listOf(
            PieChartData.Slice(
                label = "Еда",
                value = 100f,
                color = Color(0xFFE57373)
            )

        ),
        plotType = PlotType.Bar
    ),

    )

val mockTransactionUiStateLoading = AnalysisUiState.Loading

val mockTransactionUiStateError = AnalysisUiState.Error(
    message = "Ошибка загрузки данных. Проверьте подключение к интернету."
)