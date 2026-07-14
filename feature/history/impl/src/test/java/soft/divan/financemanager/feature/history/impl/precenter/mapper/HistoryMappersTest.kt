package soft.divan.financemanager.feature.history.impl.precenter.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import soft.divan.financemanager.core.domain.model.Category
import soft.divan.financemanager.core.domain.model.CurrencySymbol
import soft.divan.financemanager.core.domain.model.Transaction
import soft.divan.financemanager.core.domain.model.TransactionType
import soft.divan.financemanager.core.domain.utli.UiDateFormatter
import java.math.BigDecimal
import java.time.Instant

class HistoryMappersTest {

    private val category = Category(
        id = "cat-1",
        createdAt = Instant.EPOCH,
        updatedAt = Instant.EPOCH,
        name = "Food",
        emoji = "🍔",
        isIncome = false
    )

    private val transaction = Transaction(
        id = "t1",
        accountLocalId = "a1",
        targetAccountLocalId = null,
        currencyId = CurrencySymbol.RUB.id,
        categoryId = "cat-1",
        amount = BigDecimal("100.50"),
        type = TransactionType.EXPENSE,
        transactionDate = Instant.parse("2024-03-07T12:30:00Z"),
        comment = "lunch",
        createdAt = Instant.EPOCH,
        updatedAt = Instant.EPOCH
    )

    @Test
    fun `Category toUi keeps name and emoji`() {
        val ui = category.toUi()

        assertThat(ui.name).isEqualTo("Food")
        assertThat(ui.emoji).isEqualTo("🍔")
    }

    @Test
    fun `Transaction toUi formats amount with currency symbol`() {
        val ui = transaction.toUi(category)

        assertThat(ui.id).isEqualTo("t1")
        assertThat(ui.amountFormatted).isEqualTo("100.5 ₽")
        assertThat(ui.category.name).isEqualTo("Food")
        assertThat(ui.comment).isEqualTo("lunch")
    }

    @Test
    fun `Transaction toUi strips trailing zeros`() {
        val ui = transaction.copy(amount = BigDecimal("200.00")).toUi(category)

        assertThat(ui.amountFormatted).isEqualTo("200 ₽")
    }

    @Test
    fun `Transaction toUi keeps unknown currency id as is`() {
        val ui = transaction.copy(currencyId = "custom-id").toUi(category)

        assertThat(ui.amountFormatted).isEqualTo("100.5 custom-id")
    }

    @Test
    fun `Transaction toUi formats date via UiDateFormatter`() {
        val ui = transaction.toUi(category)

        assertThat(ui.transactionDateTime)
            .isEqualTo(UiDateFormatter.formatDateTime(transaction.transactionDate))
    }

    @Test
    fun `Transaction toUi renders null comment as string`() {
        // Текущее поведение: comment.toString() превращает null в строку "null"
        val ui = transaction.copy(comment = null).toUi(category)

        assertThat(ui.comment).isEqualTo("null")
    }
}
