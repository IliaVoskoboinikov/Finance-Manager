package soft.divan.financemanager.core.domain.usecase.impl

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import soft.divan.financemanager.core.domain.model.Transaction
import java.math.BigDecimal
import java.time.Instant

class GetSumTransactionsUseCaseImplTest {

    private val useCase = GetSumTransactionsUseCaseImpl()

    private fun transaction(amount: String): Transaction = Transaction(
        id = "id-$amount",
        accountLocalId = "acc",
        currencyCode = "RUB",
        categoryId = 1,
        amount = BigDecimal(amount),
        transactionDate = Instant.EPOCH,
        comment = null,
        createdAt = Instant.EPOCH,
        updatedAt = Instant.EPOCH
    )

    @Test
    fun `invoke returns zero for empty list`() {
        assertThat(useCase(emptyList())).isEqualByComparingTo(BigDecimal.ZERO)
    }

    @Test
    fun `invoke sums all amounts`() {
        val transactions = listOf(
            transaction("100.50"),
            transaction("200.25"),
            transaction("0.25")
        )

        assertThat(useCase(transactions)).isEqualByComparingTo(BigDecimal("301.00"))
    }

    @Test
    fun `invoke handles negative amounts`() {
        val transactions = listOf(
            transaction("100"),
            transaction("-40"),
            transaction("-10")
        )

        assertThat(useCase(transactions)).isEqualByComparingTo(BigDecimal("50"))
    }

    @Test
    fun `invoke preserves scale precision`() {
        val transactions = listOf(
            transaction("0.1"),
            transaction("0.2")
        )

        assertThat(useCase(transactions)).isEqualByComparingTo(BigDecimal("0.3"))
    }

    @Test
    fun `invoke returns the single amount for one transaction`() {
        assertThat(useCase(listOf(transaction("999.99"))))
            .isEqualByComparingTo(BigDecimal("999.99"))
    }
}
