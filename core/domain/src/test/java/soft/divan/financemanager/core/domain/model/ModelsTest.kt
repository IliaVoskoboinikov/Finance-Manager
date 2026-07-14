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
            currencyId = "rub-id",
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
        val a = transaction(date)
        val b = transaction(date)

        assertThat(a).isEqualTo(b)
        assertThat(a.hashCode()).isEqualTo(b.hashCode())
    }

    @Test
    fun `Transaction allows null comment`() {
        val transaction = transaction(Instant.EPOCH, comment = null)

        assertThat(transaction.comment).isNull()
    }

    @Test
    fun `Category exposes its fields`() {
        val category = Category(
            id = "5",
            createdAt = Instant.EPOCH,
            updatedAt = Instant.EPOCH,
            name = "Food",
            emoji = "🍔",
            isIncome = false
        )

        assertThat(category.id).isEqualTo("5")
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

    @Test
    fun `Currency equality is structural`() {
        val a = Currency(id = "rub-id", name = "Российский рубль")
        val b = Currency(id = "rub-id", name = "Российский рубль")

        assertThat(a).isEqualTo(b)
        assertThat(a.copy(name = "Ruble")).isNotEqualTo(a)
    }

    @Test
    fun `TransactionType contains all expected operation kinds`() {
        assertThat(TransactionType.entries).containsExactly(
            TransactionType.INCOME,
            TransactionType.EXPENSE,
            TransactionType.ADJUSTMENT,
            TransactionType.TRANSFER_IN,
            TransactionType.TRANSFER_OUT
        )
    }

    @Test
    fun `Transaction transfer carries target account`() {
        val transfer = transaction(Instant.EPOCH).copy(
            type = TransactionType.TRANSFER_OUT,
            targetAccountLocalId = "target-acc"
        )

        assertThat(transfer.targetAccountLocalId).isEqualTo("target-acc")
        assertThat(transfer.type).isEqualTo(TransactionType.TRANSFER_OUT)
    }

    private fun transaction(date: Instant, comment: String? = "note") = Transaction(
        id = "1",
        accountLocalId = "acc",
        targetAccountLocalId = null,
        currencyId = "rub-id",
        categoryId = "2",
        amount = BigDecimal.ONE,
        type = TransactionType.EXPENSE,
        transactionDate = date,
        comment = comment,
        createdAt = date,
        updatedAt = date
    )
}
