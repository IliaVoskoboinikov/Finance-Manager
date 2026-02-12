package soft.divan.financemanager.feature.history.impl.precenter.model

import java.time.LocalDate

data class HistoryActions(
    val onRetry: () -> Unit,
    val onUpdateStartDate: (LocalDate) -> Unit,
    val onUpdateEndDate: (LocalDate) -> Unit,
    val onNavigateToTransaction: (String) -> Unit,
    val onNavigateBack: () -> Unit,
    val onNavigateToAnalysis: () -> Unit
)
