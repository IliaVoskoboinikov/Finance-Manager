package soft.divan.financemanager.feature.history.impl.precenter.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

class HistoryModelsSmokeTest {

    @Test
    fun `actions invoke provided handlers`() {
        var calls = 0
        val actions = HistoryActions(
            onRetry = { calls++ },
            onUpdateStartDate = { calls++ },
            onUpdateEndDate = { calls++ },
            onNavigateToTransaction = { calls++ },
            onNavigateBack = { calls++ },
            onNavigateToAnalysis = { calls++ }
        )

        actions.onRetry()
        actions.onUpdateStartDate(LocalDate.now())
        actions.onUpdateEndDate(LocalDate.now())
        actions.onNavigateToTransaction("t1")
        actions.onNavigateBack()
        actions.onNavigateToAnalysis()

        assertThat(calls).isEqualTo(6)
    }

    @Test
    fun `preview mock data covers all ui states`() {
        assertThat(mockHistoryUiStateLoading).isEqualTo(HistoryUiState.Loading)
        assertThat(mockHistoryUiStateSuccess.transactions).isEqualTo(testUiTransactions)
        assertThat(mockHistoryUiStateSuccess.transactions).isNotEmpty()
        assertThat(mockTHistoryUiStateError).isInstanceOf(HistoryUiState.Error::class.java)
    }

    @Test
    fun `mock transactions reference mock categories`() {
        assertThat(testUiCategories).isNotEmpty()
        testUiTransactions.forEach { transaction ->
            assertThat(testUiCategories).contains(transaction.category)
        }
    }
}
