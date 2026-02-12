package soft.divan.financemanager.feature.analysis.impl.precenter.model

import java.time.LocalDate

data class AnalysisActions(
    val onRetry: () -> Unit,
    val onNavigateBack: () -> Unit,
    val onUpdateStartDate: (LocalDate) -> Unit,
    val onUpdateEndDate: (LocalDate) -> Unit
)
