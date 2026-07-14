package soft.divan.financemanager.feature.transaction.impl.precenter.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class TransactionModelsSmokeTest {

    @Test
    fun `actions invoke provided handlers`() {
        var calls = 0
        val actions = TransactionActions(
            onNavigateBack = { calls++ },
            onSave = { calls++ },
            onAmountChange = { calls++ },
            onCommentChange = { calls++ },
            onDateChange = { calls++ },
            onTimeChange = { calls++ },
            onCategoryChange = { calls++ },
            onAccountChange = { calls++ },
            onDelete = { calls++ }
        )

        actions.onNavigateBack()
        actions.onSave()
        actions.onAmountChange("1")
        actions.onCommentChange("c")
        actions.onDateChange(LocalDate.now())
        actions.onTimeChange(LocalTime.NOON)
        actions.onCategoryChange(mockCategories.first())
        actions.onAccountChange(mockAccounts.first())
        actions.onDelete()

        assertThat(calls).isEqualTo(9)
    }

    @Test
    fun `preview mock data covers all ui states`() {
        assertThat(mockTransactionUiStateLoading).isEqualTo(TransactionUiState.Loading)
        assertThat(mockTransactionUiStateSuccess.transaction).isEqualTo(mockTransaction)
        assertThat(mockTransactionUiStateSuccess.categories).isNotEmpty()
        assertThat(mockTransactionUiStateSuccess.accounts).isNotEmpty()
        assertThat(mockTransactionUiStateError).isInstanceOf(TransactionUiState.Error::class.java)
    }
}
