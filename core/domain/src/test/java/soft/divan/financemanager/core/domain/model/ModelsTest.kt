package soft.divan.financemanager.core.domain.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.math.BigDecimal
import java.time.Instant

class ModelsTest {

    @Test
    fun `Account copy changes only the requested field`() {
        val account = Account(
            id = "1",
            name = "Cash",
            balance = BigDecimal.TEN,
            currency = "RUB",
            createdAt = Instant.EPOCH,
            updatedAt = Instant.EPOCH
        )

        val renamed = account.copy(name = "Card")

        assertThat(renamed.name).isEqualTo("Card")
        assertThat(renamed.balance).isEqualTo(account.balance)
        assertThat(renamed).isNotEqualTo(account)
    }

    @Test
    fun `Transaction equality is structural`() {
        val date = Instant.parse("2024-01-01T00:00:00Z")
        val a = Transaction("1", "acc", "RUB", 2, BigDecimal.ONE, date, "note", date, date)
        val b = Transaction("1", "acc", "RUB", 2, BigDecimal.ONE, date, "note", date, date)

        assertThat(a).isEqualTo(b)
        assertThat(a.hashCode()).isEqualTo(b.hashCode())
    }

    @Test
    fun `Transaction allows null comment`() {
        val transaction = Transaction(
            id = "1",
            accountLocalId = "acc",
            currencyCode = "RUB",
            categoryId = 1,
            amount = BigDecimal.ONE,
            transactionDate = Instant.EPOCH,
            comment = null,
            createdAt = Instant.EPOCH,
            updatedAt = Instant.EPOCH
        )

        assertThat(transaction.comment).isNull()
    }

    @Test
    fun `Category exposes its fields`() {
        val category = Category(id = 5, name = "Food", emoji = "🍔", isIncome = false)

        assertThat(category.id).isEqualTo(5)
        assertThat(category.isIncome).isFalse()
    }

    @Test
    fun `Period holds start and end`() {
        val start = Instant.parse("2024-01-01T00:00:00Z")
        val end = Instant.parse("2024-01-31T00:00:00Z")

        val period = Period(start, end)

        assertThat(period.startDate).isEqualTo(start)
        assertThat(period.endDate).isEqualTo(end)
    }

    @Test
    fun `Const exposes default stop timeout`() {
        assertThat(Const.DEFAULT_STOP_TIMEOUT_MS).isEqualTo(5_000L)
    }
}
