package soft.divan.financemanager.feature.transactionstoday.impl.presenter.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TransactionsTodayModelsSmokeTest {

    @Test
    fun `actions invoke provided handlers`() {
        var calls = 0
        val actions = TransactionsTodayActions(
            onRetry = { calls++ },
            onNavigateToHistory = { calls++ },
            onNavigateToNewTransaction = { calls++ },
            onNavigateToOldTransaction = { calls++ },
            onHaptic = { calls++ }
        )

        actions.onRetry()
        actions.onNavigateToHistory()
        actions.onNavigateToNewTransaction()
        actions.onNavigateToOldTransaction("t1")
        actions.onHaptic()

        assertThat(calls).isEqualTo(5)
    }

    @Test
    fun `preview mock data covers all ui states`() {
        assertThat(mockTransactionsTodayUiStateLoading)
            .isEqualTo(TransactionsTodayUiState.Loading)
        assertThat(mockTransactionsTodayUiStateSuccess.transactions)
            .isEqualTo(testTransactionUis)
        assertThat(mockTransactionsTodayUiStateSuccess.transactions).isNotEmpty()
        assertThat(mockTransactionsTodayUiStateError)
            .isInstanceOf(TransactionsTodayUiState.Error::class.java)
    }

    @Test
    fun `mock transactions reference mock categories`() {
        assertThat(testUiCategories).isNotEmpty()
        testTransactionUis.forEach { transaction ->
            assertThat(testUiCategories).contains(transaction.category)
        }
    }
}
