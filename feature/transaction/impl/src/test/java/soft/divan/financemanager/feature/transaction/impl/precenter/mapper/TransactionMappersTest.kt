package soft.divan.financemanager.feature.transaction.impl.precenter.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import soft.divan.financemanager.core.domain.model.Account
import soft.divan.financemanager.core.domain.model.Category
import soft.divan.financemanager.core.domain.model.Transaction
import soft.divan.financemanager.core.domain.model.TransactionType
import soft.divan.financemanager.feature.transaction.impl.precenter.model.CategoryUi
import soft.divan.financemanager.feature.transaction.impl.precenter.model.TransactionMode
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.ZoneId

class TransactionMappersTest {

    private val zone: ZoneId = ZoneId.systemDefault()

    private val categoryUi = CategoryUi(id = "cat-1", name = "Food", emoji = "🍔")

    private val transaction = Transaction(
        id = "t1",
        accountLocalId = "a1",
        targetAccountLocalId = null,
        currencyId = "rub-id",
        categoryId = "cat-1",
        amount = BigDecimal("100.50"),
        type = TransactionType.EXPENSE,
        transactionDate = LocalDateTime.of(2024, 3, 7, 12, 30).atZone(zone).toInstant(),
        comment = "lunch",
        createdAt = LocalDateTime.of(2024, 1, 1, 10, 0).atZone(zone).toInstant(),
        updatedAt = LocalDateTime.of(2024, 2, 1, 11, 0).atZone(zone).toInstant()
    )

    @Test
    fun `Account toUi maps fields`() {
        val account = Account(
            id = "a1",
            name = "Cash",
            balance = BigDecimal("100.50"),
            currencyId = "rub-id",
            createdAt = transaction.createdAt,
            updatedAt = transaction.updatedAt
        )

        val ui = account.toUi()

        assertThat(ui.id).isEqualTo("a1")
        assertThat(ui.name).isEqualTo("Cash")
        assertThat(ui.balance).isEqualTo("100.50")
        assertThat(ui.currencyId).isEqualTo("rub-id")
        assertThat(ui.archived).isFalse()
    }

    @Test
    fun `Account toUi marks archived when requested`() {
        val account = Account(
            id = "a1",
            name = "Cash",
            balance = BigDecimal("100.50"),
            currencyId = "rub-id",
            createdAt = transaction.createdAt,
            updatedAt = transaction.updatedAt
        )

        assertThat(account.toUi(archived = true).archived).isTrue()
    }

    @Test
    fun `Category toUi maps fields`() {
        val category = Category(
            id = "cat-1",
            createdAt = transaction.createdAt,
            updatedAt = transaction.updatedAt,
            name = "Food",
            emoji = "🍔",
            isIncome = false
        )

        assertThat(category.toUi()).isEqualTo(categoryUi)
    }

    @Test
    fun `Transaction toUi splits date and time and sets edit mode`() {
        val ui = transaction.toUi(categoryUi)

        assertThat(ui.id).isEqualTo("t1")
        assertThat(ui.accountId).isEqualTo("a1")
        assertThat(ui.amount).isEqualTo("100.5")
        assertThat(ui.date).isEqualTo("07.03.2024")
        assertThat(ui.time).isEqualTo("12:30")
        assertThat(ui.comment).isEqualTo("lunch")
        assertThat(ui.mode).isEqualTo(TransactionMode.Edit("t1"))
        assertThat(ui.type).isEqualTo(TransactionType.EXPENSE)
    }

    @Test
    fun `ui to domain round-trip preserves values up to minutes precision`() {
        val roundTrip = transaction.toUi(categoryUi).toDomain()

        assertThat(roundTrip.id).isEqualTo(transaction.id)
        assertThat(roundTrip.accountLocalId).isEqualTo(transaction.accountLocalId)
        assertThat(roundTrip.categoryId).isEqualTo(transaction.categoryId)
        assertThat(roundTrip.amount).isEqualByComparingTo(transaction.amount)
        assertThat(roundTrip.transactionDate).isEqualTo(transaction.transactionDate)
        assertThat(roundTrip.createdAt).isEqualTo(transaction.createdAt)
        assertThat(roundTrip.updatedAt).isEqualTo(transaction.updatedAt)
        assertThat(roundTrip.type).isEqualTo(transaction.type)
    }
}
