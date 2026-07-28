package soft.divan.financemanager.feature.transaction.impl.domain.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import soft.divan.financemanager.core.domain.model.TransactionType
import java.math.BigDecimal

class AccountBalanceCalculatorTest {

    private val balance = BigDecimal("100")
    private val amount = BigDecimal("30")

    @Test
    fun `income and transfer-in increase balance`() {
        assertThat(AccountBalanceCalculator.calculate(balance, amount, TransactionType.INCOME))
            .isEqualByComparingTo("130")
        assertThat(AccountBalanceCalculator.calculate(balance, amount, TransactionType.TRANSFER_IN))
            .isEqualByComparingTo("130")
    }

    @Test
    fun `expense and transfer-out decrease balance`() {
        assertThat(AccountBalanceCalculator.calculate(balance, amount, TransactionType.EXPENSE))
            .isEqualByComparingTo("70")
        assertThat(
            AccountBalanceCalculator.calculate(balance, amount, TransactionType.TRANSFER_OUT)
        ).isEqualByComparingTo("70")
    }

    @Test
    fun `adjustment adds signed amount`() {
        assertThat(AccountBalanceCalculator.calculate(balance, amount, TransactionType.ADJUSTMENT))
            .isEqualByComparingTo("130")
        assertThat(
            AccountBalanceCalculator.calculate(
                balance,
                amount.negate(),
                TransactionType.ADJUSTMENT
            )
        ).isEqualByComparingTo("70")
    }

    @Test
    fun `reverting flips the operation`() {
        assertThat(
            AccountBalanceCalculator.calculate(
                balance,
                amount,
                TransactionType.INCOME,
                isReverting = true
            )
        ).isEqualByComparingTo("70")
        assertThat(
            AccountBalanceCalculator.calculate(
                balance,
                amount,
                TransactionType.EXPENSE,
                isReverting = true
            )
        ).isEqualByComparingTo("130")
    }

    @Test
    fun `applying then reverting restores original balance`() {
        TransactionType.entries.forEach { type ->
            val applied = AccountBalanceCalculator.calculate(balance, amount, type)
            val reverted = AccountBalanceCalculator.calculate(
                applied,
                amount,
                type,
                isReverting = true
            )

            assertThat(reverted).isEqualByComparingTo(balance)
        }
    }
}
