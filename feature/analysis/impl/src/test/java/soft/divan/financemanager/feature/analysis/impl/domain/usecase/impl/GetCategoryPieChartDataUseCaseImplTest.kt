package soft.divan.financemanager.feature.analysis.impl.domain.usecase.impl

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import soft.divan.financemanager.core.domain.model.Category
import soft.divan.financemanager.core.domain.model.Transaction
import soft.divan.financemanager.core.domain.model.TransactionType
import java.math.BigDecimal
import java.time.Instant

class GetCategoryPieChartDataUseCaseImplTest {

    private val useCase = GetCategoryPieChartDataUseCaseImpl()

    private val food = category(id = "food", name = "Food", emoji = "🍔")
    private val transport = category(id = "transport", name = "Transport", emoji = "🚌")

    @Test
    fun `builds slices with sums and percentages per category`() {
        val transactions = listOf(
            transaction(categoryId = "food", amount = "75"),
            transaction(categoryId = "transport", amount = "25")
        )

        val slices = useCase(transactions, listOf(food, transport))

        assertThat(slices).hasSize(2)
        val foodSlice = slices.single { it.categoryId == "food" }
        assertThat(foodSlice.categoryName).isEqualTo("Food")
        assertThat(foodSlice.amount).isEqualByComparingTo(BigDecimal("75"))
        assertThat(foodSlice.percentage).isEqualTo(75f)
    }

    @Test
    fun `skips transactions whose category is unknown instead of crashing`() {
        val transactions = listOf(
            transaction(categoryId = "food", amount = "50"),
            transaction(categoryId = "deleted-category", amount = "50")
        )

        val slices = useCase(transactions, listOf(food))

        assertThat(slices).hasSize(1)
        assertThat(slices.single().categoryId).isEqualTo("food")
    }

    @Test
    fun `returns empty list when there are no transactions`() {
        assertThat(useCase(emptyList(), listOf(food))).isEmpty()
    }

    @Test
    fun `returns empty list when total amount is zero`() {
        val transactions = listOf(transaction(categoryId = "food", amount = "0"))

        assertThat(useCase(transactions, listOf(food))).isEmpty()
    }

    @Test
    fun `uses absolute amounts for sums`() {
        val transactions = listOf(
            transaction(categoryId = "food", amount = "-30"),
            transaction(categoryId = "food", amount = "70")
        )

        val slices = useCase(transactions, listOf(food))

        assertThat(slices.single().amount).isEqualByComparingTo(BigDecimal("100"))
    }

    private fun category(id: String, name: String, emoji: String) = Category(
        id = id,
        createdAt = Instant.EPOCH,
        updatedAt = Instant.EPOCH,
        name = name,
        emoji = emoji,
        isIncome = false
    )

    private fun transaction(categoryId: String, amount: String) = Transaction(
        id = "t-$categoryId-$amount",
        accountLocalId = "account-1",
        targetAccountLocalId = null,
        currencyId = "currency-1",
        categoryId = categoryId,
        amount = BigDecimal(amount),
        type = TransactionType.EXPENSE,
        transactionDate = Instant.EPOCH,
        comment = null,
        createdAt = Instant.EPOCH,
        updatedAt = Instant.EPOCH
    )
}
