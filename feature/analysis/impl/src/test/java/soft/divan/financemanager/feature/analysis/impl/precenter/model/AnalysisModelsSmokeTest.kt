package soft.divan.financemanager.feature.analysis.impl.precenter.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

class AnalysisModelsSmokeTest {

    @Test
    fun `actions invoke provided handlers`() {
        var calls = 0
        val actions = AnalysisActions(
            onRetry = { calls++ },
            onNavigateBack = { calls++ },
            onUpdateStartDate = { calls++ },
            onUpdateEndDate = { calls++ }
        )

        actions.onRetry()
        actions.onNavigateBack()
        actions.onUpdateStartDate(LocalDate.now())
        actions.onUpdateEndDate(LocalDate.now())

        assertThat(calls).isEqualTo(4)
    }

    @Test
    fun `preview mock data covers all ui states`() {
        assertThat(mockTransactionUiStateLoading).isEqualTo(AnalysisUiState.Loading)
        assertThat(mockTransactionUiStateSuccess.sumTransaction).isNotEmpty()
        assertThat(mockTransactionUiStateSuccess.categoryPieSlice.slices).isNotEmpty()
        assertThat(mockTransactionUiStateError).isInstanceOf(AnalysisUiState.Error::class.java)
    }
}
